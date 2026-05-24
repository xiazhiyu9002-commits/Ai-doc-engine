package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.TableBlock;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.model.udm.content.TableContent;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 调试测试 - 理解空单元格如何被解析
 */
public class TableEmptyCellDebugTest {

    private ExtendedMarkdownDocumentParser parser;

    @BeforeEach
    public void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
    }

    @Test
    public void debugEmptyCellParsing() {
        String markdown = """
                | 参数类别 | 具体指标 | 规格详情 |
                | :--- | :--- | :--- |
                | **机械性能** | 自由度 | 6轴 |
                | | 最大负载 | 60kg |
                | | 工作半径 | 2050mm |
                """;

        System.out.println("=== 调试空单元格解析 ===");
        System.out.println("Markdown:");
        System.out.println(markdown);
        System.out.println();

        UdmDocument doc = parser.parse(markdown);
        TableBlock tableBlock = doc.getBlocks().stream()
            .filter(block -> block instanceof TableBlock)
            .map(block -> (TableBlock) block)
            .findFirst()
            .orElse(null);

        if (tableBlock == null) {
            System.out.println("错误：没有找到表格块");
            return;
        }

        assertNotNull(tableBlock, "应该解析出表格块");
        assertEquals(1, doc.getBlocks().stream().filter(b -> b instanceof TableBlock).count(), "应该只解析出一个表格块");

        TableContent content = tableBlock.getContent();
        System.out.println("表格内容:");
        System.out.println("  hasRichCells: " + content.hasRichCells());
        System.out.println("  headers: " + content.getHeaders());
        System.out.println("  rows: " + content.getRows());
        System.out.println();

        if (content.hasRichCells()) {
            System.out.println("富文本模式:");
            System.out.println("  headerCells size: " + content.getHeaderCells().size());
            System.out.println("  rowCells size: " + content.getRowCells().size());
            System.out.println();

            List<List<List<RichText>>> rowCells = content.getRowCells();
            for (int i = 0; i < rowCells.size(); i++) {
                List<List<RichText>> row = rowCells.get(i);
                System.out.println("  行 " + i + " (单元格数: " + row.size() + "):");
                
                for (int j = 0; j < row.size(); j++) {
                    List<RichText> cell = row.get(j);
                    System.out.println("    单元格 " + j + ":");
                    
                    if (cell.isEmpty()) {
                        System.out.println("      [空列表]");
                    } else {
                        for (RichText rt : cell) {
                            System.out.println("      text: '" + rt.getText() + "'");
                            System.out.println("      bold: " + rt.getBold());
                            System.out.println("      formula: " + rt.getInlineFormula());
                        }
                    }
                }
                System.out.println();
            }
        }

        // 检查简单模式
        System.out.println("简单模式:");
        List<List<String>> rows = content.getRows();
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            System.out.println("  行 " + i + ": " + row);
            for (int j = 0; j < row.size(); j++) {
                String cell = row.get(j);
                System.out.println("    单元格 " + j + ": '" + cell + "' (isEmpty: " + cell.isEmpty() + ", isBlank: " + cell.isBlank() + ")");
            }
        }

        assertTrue(rows.size() >= 3, "应该解析出至少3行数据（包含空单元格行）");
    }
}
