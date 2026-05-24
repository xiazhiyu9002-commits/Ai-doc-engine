package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.CodeBlock;
import com.aidoc.engine.model.udm.block.FlowchartBlock;
import com.aidoc.engine.model.udm.block.FootnoteBlock;
import com.aidoc.engine.model.udm.block.FormulaBlock;
import com.aidoc.engine.model.udm.block.HeadingBlock;
import com.aidoc.engine.model.udm.block.ParagraphBlock;
import com.aidoc.engine.model.udm.block.TaskListBlock;
import com.aidoc.engine.model.udm.content.*;
import com.aidoc.engine.parser.DocumentParser;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.util.TextNormalizer;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Primary
public class ExtendedMarkdownDocumentParser implements DocumentParser {

    private final Parser parser;
    private final MermaidParserService mermaidParserService;

    private static final Pattern TASK_LIST_PATTERN = Pattern.compile("^(\\s*)- \\[([ xX])\\]\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern FOOTNOTE_DEF_PATTERN = Pattern.compile("^\\[\\^([^\\]]+)\\]:\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern FOOTNOTE_REF_PATTERN = Pattern.compile("\\[\\^([^\\]]+)\\]");
    private static final Pattern DISPLAY_FORMULA_PATTERN = Pattern.compile("\\\\\\[([\\s\\S]*?)\\\\\\]", Pattern.MULTILINE);

    @Autowired
    public ExtendedMarkdownDocumentParser(MermaidParserService mermaidParserService) {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create()
                ))
                .set(Parser.HARD_LINE_BREAK_LIMIT, false);

        this.parser = Parser.builder(options).build();
        this.mermaidParserService = mermaidParserService;
    }

    @Override
    public UdmDocument parse(String source) {
        log.info("开始解析 Markdown 文档（扩展模式），长度: {}", source.length());

        source = decodeHtmlEntities(source);
        source = preprocessTables(source);
        
        // 预处理 \[...\] 格式的公式块，替换为占位符
        List<String> extractedFormulas = new ArrayList<>();
        String processedSource = preprocessDisplayFormulas(source, extractedFormulas);

        List<UdmBlock> blocks = new ArrayList<>();
        List<UdmBlock> preprocessedBlocks = preprocessSpecialBlocks(processedSource);
        blocks.addAll(preprocessedBlocks);

        Node document = parser.parse(processedSource);

        for (Node node : document.getChildren()) {
            UdmBlock block = parseNode(node);
            if (block != null) {
                // 检查是否是公式占位符段落
                if (block instanceof ParagraphBlock) {
                    ParagraphBlock paraBlock = (ParagraphBlock) block;
                    ParagraphContent content = paraBlock.getContent();
                    
                    // 获取段落文本（支持纯文本和富文本两种模式）
                    String text = null;
                    if (content.getText() != null) {
                        text = content.getText().trim();
                    } else if (content.getSegments() != null && !content.getSegments().isEmpty()) {
                        text = extractPlainText(content.getSegments()).trim();
                    }
                    
                    // 检查是否匹配占位符模式
                    if (text != null && text.matches("@@@FORMULA_PLACEHOLDER_(\\d+)@@@")) {
                        int index = Integer.parseInt(text.replaceAll("@@@FORMULA_PLACEHOLDER_(\\d+)@@@", "$1"));
                        if (index < extractedFormulas.size()) {
                            String latex = extractedFormulas.get(index);
                            log.debug("恢复块级公式 (\\[\\]): {}", latex.substring(0, Math.min(50, latex.length())));
                            blocks.add(createFormulaBlock(latex, false));
                            continue;
                        }
                    }
                }
                blocks.add(block);
            }
        }

        log.info("Markdown 解析完成，生成 {} 个 Block", blocks.size());

        return UdmDocument.builder()
                .blocks(blocks)
                .build();
    }

    private List<UdmBlock> preprocessSpecialBlocks(String source) {
        List<UdmBlock> blocks = new ArrayList<>();

        List<TaskListContent.TaskItem> taskItems = parseTaskListItems(source);
        if (!taskItems.isEmpty()) {
            TaskListContent content = TaskListContent.of(taskItems);
            blocks.add(new TaskListBlock(content));
            log.debug("识别到任务列表，共 {} 项", taskItems.size());
        }

        List<FootnoteContent> footnotes = parseFootnotes(source);
        for (FootnoteContent footnote : footnotes) {
            blocks.add(new FootnoteBlock(footnote));
            log.debug("识别到脚注: {}", footnote.getId());
        }

        return blocks;
    }
    
    /**
     * 预处理 \[...\] 格式的显示公式，替换为占位符
     */
    private String preprocessDisplayFormulas(String source, List<String> extractedFormulas) {
        Matcher matcher = DISPLAY_FORMULA_PATTERN.matcher(source);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String latex = matcher.group(1).trim();
            int index = extractedFormulas.size();
            extractedFormulas.add(latex);
            
            // 替换为占位符，使用特殊格式避免被 Markdown 解析器处理
            // 使用 @@@ 而不是 __ 避免被解释为格式标记
            String placeholder = "\n\n@@@FORMULA_PLACEHOLDER_" + index + "@@@\n\n";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
            
            log.debug("提取块级公式 #{}: {}", index, latex.substring(0, Math.min(50, latex.length())));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }

    private List<TaskListContent.TaskItem> parseTaskListItems(String source) {
        List<TaskListContent.TaskItem> items = new ArrayList<>();
        Matcher matcher = TASK_LIST_PATTERN.matcher(source);

        while (matcher.find()) {
            String indent = matcher.group(1);
            boolean checked = !matcher.group(2).trim().isEmpty();
            String text = matcher.group(3).trim();

            TaskListContent.TaskItem item = TaskListContent.TaskItem.builder()
                    .text(text)
                    .checked(checked)
                    .build();

            items.add(item);
        }

        return items;
    }

    private List<FootnoteContent> parseFootnotes(String source) {
        List<FootnoteContent> footnotes = new ArrayList<>();
        Matcher matcher = FOOTNOTE_DEF_PATTERN.matcher(source);

        while (matcher.find()) {
            String id = matcher.group(1);
            String text = matcher.group(2).trim();

            footnotes.add(FootnoteContent.of(id, text));
        }

        return footnotes;
    }

    private UdmBlock parseNode(Node node) {
        log.debug("解析节点类型: {}", node.getClass().getSimpleName());

        UdmBlock block = null;
        
        if (node instanceof Heading) {
            block = parseHeading((Heading) node);
        } else if (node instanceof Paragraph) {
            block = parseParagraph((Paragraph) node);
        } else if (node instanceof BulletList) {
            block = parseBulletList((BulletList) node);
        } else if (node instanceof OrderedList) {
            block = parseOrderedList((OrderedList) node);
        } else if (node instanceof TableBlock) {
            log.info("识别到表格节点");
            block = parseTable((TableBlock) node);
        } else if (node instanceof FencedCodeBlock) {
            block = parseFencedCodeBlock((FencedCodeBlock) node);
        } else if (node instanceof BlockQuote) {
            block = parseBlockQuote((BlockQuote) node);
        } else if (node instanceof ThematicBreak) {
            block = parseThematicBreak();
        }

        if (block != null) {
            block.setSourceStartOffset(node.getStartOffset());
            block.setSourceEndOffset(node.getEndOffset());
            block.setSourceStartLine(node.getStartLineNumber() + 1);
            block.setSourceEndLine(node.getEndLineNumber() + 1);
        } else {
            log.debug("未处理的节点类型: {}", node.getClass().getSimpleName());
        }

        return block;
    }

    private HeadingBlock parseHeading(Heading heading) {
        List<RichText> segments = parseRichText(heading);
        String text = extractPlainText(segments);
        
        // 清理标题文本：移除多余空格和换行符
        text = TextNormalizer.cleanHeadingText(text);

        HeadingContent content = HeadingContent.builder()
                .level(heading.getLevel())
                .text(text)
                .build();

        return new HeadingBlock(content);
    }

    private UdmBlock parseParagraph(Paragraph paragraph) {
        String text = paragraph.getContentChars().toString().trim();

        if (text.startsWith("$$") && text.endsWith("$$") && text.length() > 4) {
            String latex = text.substring(2, text.length() - 2).trim();
            log.debug("识别到块级公式 ($$): {}", latex);
            return createFormulaBlock(latex, false);
        } else if (text.startsWith("\\[") && text.endsWith("\\]")) {
            String latex = text.substring(2, text.length() - 2).trim();
            log.debug("识别到块级公式 (\\[\\]): {}", latex);
            return createFormulaBlock(latex, false);
        }

        List<RichText> segments = parseRichText(paragraph);

        if (segments.size() == 1 && segments.get(0).getInlineFormula() != null) {
            log.debug("段落仅包含一个公式，将其转换为块级公式");
            return createFormulaBlock(segments.get(0).getInlineFormula(), false);
        }

        boolean hasComplexFormat = segments.stream()
                .anyMatch(s -> s.getBold() || s.getItalic() || s.getCode()
                        || s.getLinkUrl() != null || s.getInlineFormula() != null);

        if (hasComplexFormat) {
            ParagraphContent content = ParagraphContent.ofRichText(segments);
            return new ParagraphBlock(content);
        }

        ParagraphContent content = ParagraphContent.of(text);
        return new ParagraphBlock(content);
    }

    private List<RichText> parseRichText(Node node) {
        List<RichText> segments = new ArrayList<>();

        String fullText = node.getChars().toString();
        List<RichText> formulaSegments = parseInlineFormulas(fullText);
        boolean hasFormula = formulaSegments.stream().anyMatch(s -> s.getInlineFormula() != null);

        if (!hasFormula) {
            for (Node child : node.getChildren()) {
                if (child instanceof Text) {
                    Text textNode = (Text) child;
                    String text = textNode.getChars().toString();
                    List<RichText> parsed = parseInlineFormulas(text);
                    segments.addAll(parsed);
                } else if (child instanceof StrongEmphasis) {
                    StrongEmphasis strong = (StrongEmphasis) child;
                    List<RichText> nested = parseRichTextWithFormat(strong, true, false);
                    segments.addAll(nested);
                } else if (child instanceof Emphasis) {
                    Emphasis emphasis = (Emphasis) child;
                    List<RichText> nested = parseRichTextWithFormat(emphasis, false, true);
                    segments.addAll(nested);
                } else if (child instanceof Code) {
                    Code code = (Code) child;
                    segments.add(RichText.code(code.getChars().toString()));
                } else if (child instanceof Link) {
                    Link link = (Link) child;
                    String linkText = extractPlainText(link);
                    String url = link.getUrl().toString();
                    segments.add(RichText.link(linkText, url));
                } else if (child instanceof SoftLineBreak || child instanceof HardLineBreak) {
                    segments.add(RichText.of("\n"));
                } else if (child instanceof TextBase) {
                    TextBase textBase = (TextBase) child;
                    String text = textBase.getChars().toString();
                    List<RichText> parsed = parseInlineFormulas(text);
                    segments.addAll(parsed);
                } else {
                    String text = child.getChars().toString();
                    if (!text.isEmpty()) {
                        List<RichText> parsed = parseInlineFormulas(text);
                        segments.addAll(parsed);
                    }
                }
            }

            if (segments.isEmpty()) {
                segments = formulaSegments;
            }
        } else {
            int nodeStartOffset = node.getStartOffset();
            List<int[]> formatRanges = new ArrayList<>();
            for (Node child : node.getChildren()) {
                if (child instanceof StrongEmphasis) {
                    int start = child.getStartOffset() - nodeStartOffset;
                    int end = child.getEndOffset() - nodeStartOffset;
                    formatRanges.add(new int[]{start, end, 1});
                } else if (child instanceof Emphasis) {
                    int start = child.getStartOffset() - nodeStartOffset;
                    int end = child.getEndOffset() - nodeStartOffset;
                    formatRanges.add(new int[]{start, end, 2});
                }
            }

            if (formatRanges.isEmpty()) {
                segments = formulaSegments;
            } else {
                int currentPos = 0;
                for (RichText seg : formulaSegments) {
                    String text = seg.getText();
                    String formula = seg.getInlineFormula();
                    
                    int segStart = currentPos;
                    int segEnd;
                    if (formula != null) {
                        segEnd = segStart + formula.length() + 2;
                    } else {
                        segEnd = segStart + (text != null ? text.length() : 0);
                    }
                    
                    if (formula != null) {
                        segments.add(RichText.formula(formula));
                        currentPos = segEnd;
                        continue;
                    }
                    
                    List<int[]> overlappingRanges = new ArrayList<>();
                    for (int[] range : formatRanges) {
                        if (segStart < range[1] && segEnd > range[0]) {
                            overlappingRanges.add(range);
                        }
                    }
                    
                    if (overlappingRanges.isEmpty()) {
                        segments.add(RichText.of(text));
                        currentPos = segEnd;
                        continue;
                    }
                    
                    overlappingRanges.sort((a, b) -> Integer.compare(a[0], b[0]));
                    
                    int pos = segStart;
                    for (int[] range : overlappingRanges) {
                        int rangeStart = Math.max(range[0], segStart);
                        int rangeEnd = Math.min(range[1], segEnd);
                        
                        if (rangeStart > pos) {
                            String beforeText = fullText.substring(pos, rangeStart);
                            segments.add(RichText.of(beforeText));
                        }
                        
                        if (rangeStart < rangeEnd) {
                            String rangeText = fullText.substring(rangeStart, rangeEnd);
                            boolean isBold = range[2] == 1;
                            boolean isItalic = range[2] == 2;
                            
                            if (isBold && rangeText.startsWith("**") && rangeText.endsWith("**") && rangeText.length() > 4) {
                                rangeText = rangeText.substring(2, rangeText.length() - 2);
                            } else if (isItalic && rangeText.startsWith("*") && rangeText.endsWith("*") && rangeText.length() > 2) {
                                rangeText = rangeText.substring(1, rangeText.length() - 1);
                            }
                            
                            segments.add(RichText.builder()
                                .text(rangeText)
                                .bold(isBold)
                                .italic(isItalic)
                                .build());
                        }
                        
                        pos = rangeEnd;
                    }
                    
                    if (pos < segEnd) {
                        String afterText = fullText.substring(pos, segEnd);
                        segments.add(RichText.of(afterText));
                    }
                    
                    currentPos = segEnd;
                }
            }
        }

        return segments;
    }

    private List<RichText> parseInlineFormulas(String text) {
        List<RichText> segments = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return segments;
        }
        
        // 将 <br> 标签转换为换行符
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");

        Pattern pattern = Pattern.compile(
                "(\\$\\$(.+?)\\$\\$)|((?<!\\\\)\\$([^$\\n]+?)(?<!\\\\)\\$)|(\\\\\\((.+?)\\\\\\))|(\\\\\\[(.+?)\\\\\\])",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String before = text.substring(lastEnd, matcher.start());
                if (!before.isEmpty()) {
                    before = before.replace("\\$", "$");
                    // 将文本按换行符拆分
                    String[] parts = before.split("\n", -1);
                    for (int i = 0; i < parts.length; i++) {
                        if (!parts[i].isEmpty()) {
                            segments.add(RichText.of(parts[i]));
                        }
                        if (i < parts.length - 1) {
                            segments.add(RichText.of("\n"));
                        }
                    }
                }
            }

            String formula;
            if (matcher.group(2) != null) {
                formula = matcher.group(2).trim();
                log.debug("识别到公式 ($$): {}", formula);
            } else if (matcher.group(4) != null) {
                formula = matcher.group(4).trim();
                log.debug("识别到行内公式 ($): {}", formula);
            } else if (matcher.group(6) != null) {
                formula = matcher.group(6).trim();
                log.debug("识别到行内公式 (\\(\\)): {}", formula);
            } else {
                formula = matcher.group(8).trim();
                log.debug("识别到公式 (\\[\\]): {}", formula);
            }

            segments.add(RichText.formula(formula));

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd);
            if (!remaining.isEmpty()) {
                remaining = remaining.replace("\\$", "$");
                // 将文本按换行符拆分
                String[] parts = remaining.split("\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        segments.add(RichText.of(parts[i]));
                    }
                    if (i < parts.length - 1) {
                        segments.add(RichText.of("\n"));
                    }
                }
            }
        }

        return segments;
    }

    private List<RichText> parseRichTextWithFormat(Node node, boolean bold, boolean italic) {
        List<RichText> segments = new ArrayList<>();

        for (Node child : node.getChildren()) {
            if (child instanceof Text) {
                Text textNode = (Text) child;
                String text = textNode.getChars().toString();
                List<RichText> parsedSegments = parseInlineFormulas(text);
                for (RichText segment : parsedSegments) {
                    RichText.RichTextBuilder builder = RichText.builder()
                            .text(segment.getText())
                            .bold(bold || (segment.getBold() != null && segment.getBold()))
                            .italic(italic || (segment.getItalic() != null && segment.getItalic()))
                            .strikethrough(segment.getStrikethrough())
                            .code(segment.getCode())
                            .linkUrl(segment.getLinkUrl())
                            .inlineFormula(segment.getInlineFormula());
                    segments.add(builder.build());
                }
            } else if (child instanceof StrongEmphasis) {
                List<RichText> nested = parseRichTextWithFormat(child, true, italic);
                segments.addAll(nested);
            } else if (child instanceof Emphasis) {
                List<RichText> nested = parseRichTextWithFormat(child, bold, true);
                segments.addAll(nested);
            } else if (child instanceof Code) {
                Code code = (Code) child;
                segments.add(RichText.builder()
                        .text(code.getChars().toString())
                        .code(true)
                        .build());
            } else if (child instanceof Link) {
                Link link = (Link) child;
                String linkText = extractPlainText(link);
                String url = link.getUrl().toString();
                segments.add(RichText.builder()
                        .text(linkText)
                        .linkUrl(url)
                        .bold(bold)
                        .italic(italic)
                        .build());
            } else {
                String text = child.getChars().toString();
                if (!text.isEmpty()) {
                    List<RichText> parsedSegments = parseInlineFormulas(text);
                    for (RichText segment : parsedSegments) {
                        RichText.RichTextBuilder builder = RichText.builder()
                                .text(segment.getText())
                                .bold(bold || (segment.getBold() != null && segment.getBold()))
                                .italic(italic || (segment.getItalic() != null && segment.getItalic()))
                                .strikethrough(segment.getStrikethrough())
                                .code(segment.getCode())
                                .linkUrl(segment.getLinkUrl())
                                .inlineFormula(segment.getInlineFormula());
                        segments.add(builder.build());
                    }
                }
            }
        }

        return segments;
    }

    private String extractPlainText(Node node) {
        StringBuilder sb = new StringBuilder();
        for (Node child : node.getChildren()) {
            if (child instanceof Text) {
                sb.append(((Text) child).getChars());
            } else if (child instanceof TextBase) {
                sb.append(((TextBase) child).getChars());
            } else {
                sb.append(extractPlainText(child));
            }
        }
        return sb.toString();
    }

    private String extractPlainText(List<RichText> segments) {
        StringBuilder sb = new StringBuilder();
        for (RichText segment : segments) {
            if (segment.getText() != null) {
                sb.append(segment.getText());
            }
        }
        return sb.toString();
    }

    private FormulaBlock createFormulaBlock(String latex, boolean inline) {
        FormulaContent content = FormulaContent.builder()
                .latex(latex)
                .inline(inline)
                .build();
        return new FormulaBlock(content);
    }

    private com.aidoc.engine.model.udm.block.ListBlock parseBulletList(BulletList bulletList) {
        List<ListItemContent> itemContents = new ArrayList<>();

        for (Node child : bulletList.getChildren()) {
            if (child instanceof BulletListItem) {
                ListItemContent itemContent = parseListItem((BulletListItem) child);
                itemContents.add(itemContent);
            }
        }

        ListContent content = ListContent.ofItems(false, itemContents);
        return new com.aidoc.engine.model.udm.block.ListBlock(content);
    }

    private com.aidoc.engine.model.udm.block.ListBlock parseOrderedList(OrderedList orderedList) {
        List<ListItemContent> itemContents = new ArrayList<>();

        for (Node child : orderedList.getChildren()) {
            if (child instanceof OrderedListItem) {
                ListItemContent itemContent = parseListItem((OrderedListItem) child);
                itemContents.add(itemContent);
            }
        }

        ListContent content = ListContent.ofItems(true, itemContents);
        return new com.aidoc.engine.model.udm.block.ListBlock(content);
    }

    private ListItemContent parseListItem(ListItem listItem) {
        List<RichText> segments = new ArrayList<>();
        ListContent nestedList = null;

        for (Node child : listItem.getChildren()) {
            if (child instanceof Paragraph) {
                List<RichText> paragraphSegments = parseRichText(child);
                segments.addAll(paragraphSegments);
            } else if (child instanceof BulletList) {
                nestedList = parseBulletListContent((BulletList) child);
            } else if (child instanceof OrderedList) {
                nestedList = parseOrderedListContent((OrderedList) child);
            }
        }

        return ListItemContent.builder()
                .segments(segments)
                .nestedList(nestedList)
                .build();
    }

    private ListContent parseBulletListContent(BulletList bulletList) {
        List<ListItemContent> itemContents = new ArrayList<>();
        for (Node child : bulletList.getChildren()) {
            if (child instanceof BulletListItem) {
                itemContents.add(parseListItem((BulletListItem) child));
            }
        }
        return ListContent.ofItems(false, itemContents);
    }

    private ListContent parseOrderedListContent(OrderedList orderedList) {
        List<ListItemContent> itemContents = new ArrayList<>();
        for (Node child : orderedList.getChildren()) {
            if (child instanceof OrderedListItem) {
                itemContents.add(parseListItem((OrderedListItem) child));
            }
        }
        return ListContent.ofItems(true, itemContents);
    }

    private com.aidoc.engine.model.udm.block.TableBlock parseTable(TableBlock tableBlock) {
        log.info("开始解析表格");

        List<List<RichText>> headerCells = new ArrayList<>();
        List<List<List<RichText>>> rowCells = new ArrayList<>();

        for (Node child : tableBlock.getChildren()) {
            if (child instanceof TableHead) {
                TableHead head = (TableHead) child;
                for (Node headChild : head.getChildren()) {
                    if (headChild instanceof TableRow) {
                        List<List<RichText>> headerRow = parseTableRowCells((TableRow) headChild);
                        headerCells = headerRow;
                    }
                }
            } else if (child instanceof TableBody) {
                TableBody body = (TableBody) child;
                for (Node bodyChild : body.getChildren()) {
                    if (bodyChild instanceof TableRow) {
                        List<List<RichText>> rowCellsList = parseTableRowCells((TableRow) bodyChild);
                        rowCells.add(rowCellsList);
                    }
                }
            }
        }

        List<String> headers = new ArrayList<>();
        for (List<RichText> headerCell : headerCells) {
            headers.add(extractPlainText(headerCell));
        }

        List<List<String>> rows = new ArrayList<>();
        for (List<List<RichText>> row : rowCells) {
            List<String> rowTexts = new ArrayList<>();
            for (List<RichText> cell : row) {
                rowTexts.add(extractPlainText(cell));
            }
            rows.add(rowTexts);
        }

        TableContent content = TableContent.builder()
                .headers(headers)
                .rows(rows)
                .headerCells(headerCells)
                .rowCells(rowCells)
                .build();

        return new com.aidoc.engine.model.udm.block.TableBlock(content);
    }

    private List<List<RichText>> parseTableRowCells(TableRow tableRow) {
        List<List<RichText>> cells = new ArrayList<>();

        for (Node child : tableRow.getChildren()) {
            if (child instanceof TableCell) {
                TableCell cell = (TableCell) child;
                
                // 解析单元格内容（支持富文本）
                List<RichText> cellContent = parseRichText(cell);
                
                // 清理 cellContent 中残留的 | 分隔符
                List<RichText> cleanedContent = new ArrayList<>();
                for (int i = 0; i < cellContent.size(); i++) {
                    RichText segment = cellContent.get(i);
                    if (segment.getText() != null) {
                        String text = segment.getText();

                        if (text != null && text.trim().equals("|")) {
                            continue;
                        }
                        
                        // 去除开头/结尾的 |（有些情况下 flexmark 会把分隔符带进 cell chars）
                        if (text != null) {
                            text = text.trim();
                            while (text.startsWith("|")) {
                                text = text.substring(1).trim();
                            }
                            while (text.endsWith("|")) {
                                text = text.substring(0, text.length() - 1).trim();
                            }
                        }
                        
                        // 只添加非空内容或包含公式的内容
                        if (!text.isEmpty() || segment.getInlineFormula() != null) {
                            RichText cleaned = RichText.builder()
                                    .text(text)
                                    .bold(segment.getBold())
                                    .italic(segment.getItalic())
                                    .code(segment.getCode())
                                    .linkUrl(segment.getLinkUrl())
                                    .inlineFormula(segment.getInlineFormula())
                                    .build();
                            cleanedContent.add(cleaned);
                        }
                    } else {
                        cleanedContent.add(segment);
                    }
                }
                
                cells.add(cleanedContent.isEmpty() ? cellContent : cleanedContent);
            }
        }

        return cells;
    }

    private UdmBlock parseFencedCodeBlock(FencedCodeBlock codeBlock) {
        String info = codeBlock.getInfo().toString().toLowerCase();
        String content = codeBlock.getContentChars().toString().trim();

        if ("mermaid".equals(info) || "flowchart".equals(info)) {
            log.debug("识别到流程图代码块: type={}", info);

            try {
                FlowchartContent flowchartContent = mermaidParserService.parse(content);
                log.debug("解析流程图成功");
                return new FlowchartBlock(flowchartContent);
            } catch (Exception e) {
                log.warn("流程图解析失败: {}", e.getMessage());
                // 返回空的流程图块
                FlowchartContent flowchartContent = FlowchartContent.builder()
                        .rawSource(content)
                        .sourceType(info)
                        .build();
                return new FlowchartBlock(flowchartContent);
            }
        }

        if ("math".equals(info) || "latex".equals(info)) {
            log.debug("识别到数学公式代码块: type={}", info);
            return createFormulaBlock(content, false);
        }

        CodeBlockContent codeContent = CodeBlockContent.builder()
                .language(info)
                .code(content)
                .build();
        return new CodeBlock(codeContent);
    }

    private UdmBlock parseBlockQuote(BlockQuote blockQuote) {
        List<RichText> segments = new ArrayList<>();

        for (Node child : blockQuote.getChildren()) {
            if (child instanceof Paragraph) {
                List<RichText> paragraphSegments = parseRichText(child);
                segments.addAll(paragraphSegments);
            }
        }

        ParagraphContent content = ParagraphContent.ofRichText(segments);
        return new ParagraphBlock(content);
    }

    private UdmBlock parseThematicBreak() {
        ParagraphContent content = ParagraphContent.of("---");
        return new ParagraphBlock(content);
    }

    private String decodeHtmlEntities(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        source = source.replace("&lt;", "<");
        source = source.replace("&gt;", ">");
        source = source.replace("&amp;", "&");
        source = source.replace("&quot;", "\"");
        source = source.replace("&apos;", "'");
        source = source.replace("&nbsp;", " ");

        return source;
    }

    private String preprocessTables(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        // 1. Normalize line endings
        source = source.replace("\r\n", "\n");

        // 2. Fix pipe spacing and join table lines that might have been split by single newlines
        String[] lines = source.split("\n", -1);
        StringBuilder result = new StringBuilder();
        boolean inTable = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            
            // Check if it's a table row or separator
            boolean isTableRow = trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length() >= 2;
            boolean isSeparator = trimmed.matches("\\|[-:| ]+\\|");

            if (isTableRow || isSeparator) {
                // Normalize || to | |
                String normalized = line;
                while (normalized.contains("||")) {
                    normalized = normalized.replace("||", "| |");
                }
                
                if (!inTable) {
                    // Start of a table, ensure empty line before
                    if (result.length() > 0 && !result.toString().endsWith("\n\n")) {
                        if (!result.toString().endsWith("\n")) {
                            result.append("\n");
                        }
                        result.append("\n");
                    }
                    inTable = true;
                }
                result.append(normalized).append("\n");
            } else {
                if (inTable && !trimmed.isEmpty()) {
                    // Potential table break but line isn't empty, check if it was just missing trailing pipe
                    if (trimmed.startsWith("|")) {
                        String fixed = trimmed + (trimmed.endsWith("|") ? "" : " |");
                        result.append(fixed).append("\n");
                        continue;
                    }
                }
                
                if (inTable && trimmed.isEmpty()) {
                    inTable = false;
                    result.append("\n");
                } else {
                    result.append(line).append("\n");
                }
            }
        }
        
        source = result.toString();

        // 3. Final cleanup of spacing around tables (using existing logic but refined)
        source = source.replaceAll("(?m)^([^|\\n].*)\\n(\\|.*\\|\\n\\|[-:| ]+\\|)", "$1\n\n$2");
        source = source.replaceAll("(?m)(\\|.*\\|)\\n([^|\\s].*)", "$1\n\n$2");

        return source;
    }
}
