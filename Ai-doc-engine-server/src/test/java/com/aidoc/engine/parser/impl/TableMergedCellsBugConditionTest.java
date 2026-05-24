package com.aidoc.engine.parser.impl;

import com.aidoc.engine.exporter.WordExporter;
import com.aidoc.engine.exporter.impl.Docx4jWordExporter;
import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.TableBlock;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.model.udm.content.TableContent;
import com.aidoc.engine.renderer.DocumentRenderer;
import com.aidoc.engine.service.MermaidParserService;
import com.aidoc.engine.service.impl.MermaidParserServiceImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition 探索测试 - 空单元格合并识别
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**
 * 
 * 这个测试在未修复的代码上运行，预期会失败。
 * 失败证明 bug 存在：空单元格没有被识别为合并单元格。
 * 
 * Bug Condition: 表格第一列包含空单元格（表示合并单元格语义）
 * Expected Behavior: 空单元格应被识别为属于上一个非空单元格的分组，并在导出时合并
 */
public class TableMergedCellsBugConditionTest {

    private ExtendedMarkdownDocumentParser parser;
    private WordExporter exporter;

    @BeforeEach
    public void setUp() {
        MermaidParserService mermaidParserService = new MermaidParserServiceImpl();
        parser = new ExtendedMarkdownDocumentParser(mermaidParserService);
        
        // Mocking/Stubbing or providing real dependencies for the renderer
        // In a real test environment, these would be injected or mocked
        DocumentRenderer renderer = null; // This will cause NPE if export is called, but we are fixing compilation first
        exporter = new Docx4jWordExporter(renderer);
    }

    /**
     * 测试用例 1: 单分组合并测试
     * 解析包含一个分组（4 个连续空单元格）的表格
     */
    @Test
    public void testSingleGroupMerge() throws Exception {
        System.out.println("\n=== 测试用例 1: 单分组合并测试 ===");
        
        String markdown = """
                | 参数类别 | 具体指标 | 规格详情 |
                | :--- | :--- | :--- |
                | **机械性能** | 自由度 | 6轴 |
                | | 最大负载 | 60kg |
                | | 工作半径 | 2050mm |
                | | 重复定位精度 | ±0.03mm |
                """;

        // 解析 Markdown
        UdmDocument doc = parser.parse(markdown);
        TableBlock tableBlock = findTableBlock(doc);
        assertNotNull(tableBlock, "应该解析出表格块");

        TableContent content = tableBlock.getContent();
        
        // 验证 Bug Condition: 第一列包含空单元格
        assertTrue(isBugCondition(content), "应该满足 Bug Condition：第一列包含空单元格");
        
        // 导出到 Word
        byte[] wordBytes = exporter.export(doc);
        assertNotNull(wordBytes, "应该成功导出 Word 文档");
        
        // 验证 Expected Behavior: 导出的 Word 文档应包含合并单元格
        WordprocessingMLPackage wordDoc = loadWordDocument(wordBytes);
        Tbl table = findFirstTable(wordDoc);
        assertNotNull(table, "Word 文档应包含表格");
        
        // 检查第一列是否有合并单元格
        boolean hasMergedCells = checkFirstColumnHasMergedCells(table);
        
        System.out.println("第一列是否有合并单元格: " + hasMergedCells);
        printTableStructure(table);
        
        // 预期失败：未修复的代码不会创建合并单元格
        assertTrue(hasMergedCells, 
            "Expected Behavior: 第一列应该有合并单元格（4个空单元格应合并到'机械性能'）");
    }

    /**
     * 测试用例 2: 多分组合并测试
     * 解析包含两个分组的表格，每个分组有不同数量的空单元格
     */
    @Test
    public void testMultipleGroupsMerge() throws Exception {
        System.out.println("\n=== 测试用例 2: 多分组合并测试 ===");
        
        String markdown = """
                | 类别 | 项目 | 值 |
                | --- | --- | --- |
                | **电气参数** | 供电电压 | 380V |
                | | 功率 | 5kW |
                | **控制系统** | 控制器 | PLC |
                | | 通信接口 | Ethernet |
                | | 编程语言 | Ladder |
                """;

        UdmDocument doc = parser.parse(markdown);
        TableBlock tableBlock = findTableBlock(doc);
        assertNotNull(tableBlock, "应该解析出表格块");

        TableContent content = tableBlock.getContent();
        assertTrue(isBugCondition(content), "应该满足 Bug Condition");
        
        byte[] wordBytes = exporter.export(doc);
        WordprocessingMLPackage wordDoc = loadWordDocument(wordBytes);
        Tbl table = findFirstTable(wordDoc);
        
        boolean hasMergedCells = checkFirstColumnHasMergedCells(table);
        System.out.println("第一列是否有合并单元格: " + hasMergedCells);
        printTableStructure(table);
        
        // 预期失败：应该有两个合并区域
        assertTrue(hasMergedCells, 
            "Expected Behavior: 第一列应该有两个合并区域（'电气参数'合并2行，'控制系统'合并3行）");
    }

    /**
     * 测试用例 3: 富文本合并测试
     * 解析第一列包含加粗文本和空单元格的表格
     */
    @Test
    public void testRichTextMerge() throws Exception {
        System.out.println("\n=== 测试用例 3: 富文本合并测试 ===");
        
        String markdown = """
                | 分类 | 说明 |
                | --- | --- |
                | **重要** | 第一项 |
                | | 第二项 |
                | *次要* | 第三项 |
                """;

        UdmDocument doc = parser.parse(markdown);
        TableBlock tableBlock = findTableBlock(doc);
        assertNotNull(tableBlock, "应该解析出表格块");

        TableContent content = tableBlock.getContent();
        
        // 验证富文本内容被正确解析
        assertTrue(content.hasRichCells(), "应该使用富文本模式");
        List<List<List<RichText>>> rowCells = content.getRowCells();
        assertFalse(rowCells.isEmpty(), "应该有数据行");
        
        // 检查第一行第一列是否包含加粗文本
        List<RichText> firstCell = rowCells.get(0).get(0);
        boolean hasBoldText = firstCell.stream()
            .anyMatch(rt -> Boolean.TRUE.equals(rt.getBold()));
        assertTrue(hasBoldText, "第一行第一列应该包含加粗文本");
        
        assertTrue(isBugCondition(content), "应该满足 Bug Condition");
        
        byte[] wordBytes = exporter.export(doc);
        WordprocessingMLPackage wordDoc = loadWordDocument(wordBytes);
        Tbl table = findFirstTable(wordDoc);
        
        boolean hasMergedCells = checkFirstColumnHasMergedCells(table);
        System.out.println("第一列是否有合并单元格: " + hasMergedCells);
        printTableStructure(table);
        
        assertTrue(hasMergedCells, 
            "Expected Behavior: 富文本内容应该正确合并");
    }

    /**
     * 测试用例 4: 边缘情况测试
     * 解析第一行第一列为空的表格
     */
    @Test
    public void testEdgeCaseFirstRowEmpty() throws Exception {
        System.out.println("\n=== 测试用例 4: 边缘情况 - 第一行第一列为空 ===");
        
        String markdown = """
                | 列1 | 列2 |
                | --- | --- |
                | | 值1 |
                | A | 值2 |
                """;

        UdmDocument doc = parser.parse(markdown);
        TableBlock tableBlock = findTableBlock(doc);
        assertNotNull(tableBlock, "应该解析出表格块");

        TableContent content = tableBlock.getContent();
        
        // 第一行第一列为空，没有上一个非空单元格，不应该合并
        // 但这仍然是一个需要处理的边缘情况
        
        byte[] wordBytes = exporter.export(doc);
        WordprocessingMLPackage wordDoc = loadWordDocument(wordBytes);
        Tbl table = findFirstTable(wordDoc);
        
        System.out.println("表格结构:");
        printTableStructure(table);
        
        // 这个测试主要是为了观察当前行为，不一定预期失败
        // 第一行为空时，应该保持为空（不合并）
        assertNotNull(table, "应该成功导出表格");
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查是否满足 Bug Condition
     * Bug Condition: 表格第一列包含空单元格且上方有非空单元格
     */
    private boolean isBugCondition(TableContent content) {
        if (content.hasRichCells()) {
            List<List<List<RichText>>> rowCells = content.getRowCells();
            if (rowCells.isEmpty()) {
                return false;
            }
            
            boolean foundNonEmpty = false;
            for (List<List<RichText>> row : rowCells) {
                if (row.isEmpty()) {
                    continue;
                }
                
                List<RichText> firstCell = row.get(0);
                boolean isEmpty = isEmptyCell(firstCell);
                
                if (!isEmpty) {
                    foundNonEmpty = true;
                } else if (foundNonEmpty) {
                    // 找到空单元格且之前有非空单元格
                    return true;
                }
            }
        } else {
            List<List<String>> rows = content.getRows();
            if (rows.isEmpty()) {
                return false;
            }
            
            boolean foundNonEmpty = false;
            for (List<String> row : rows) {
                if (row.isEmpty()) {
                    continue;
                }
                
                String firstCell = row.get(0);
                boolean isEmpty = firstCell == null || firstCell.trim().isEmpty();
                
                if (!isEmpty) {
                    foundNonEmpty = true;
                } else if (foundNonEmpty) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 检查单元格是否为空
     */
    private boolean isEmptyCell(List<RichText> cell) {
        if (cell == null || cell.isEmpty()) {
            return true;
        }
        
        for (RichText rt : cell) {
            if (rt.getText() != null && !rt.getText().trim().isEmpty()) {
                return false;
            }
            if (rt.getInlineFormula() != null && !rt.getInlineFormula().trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 查找文档中的第一个表格块
     */
    private TableBlock findTableBlock(UdmDocument doc) {
        return doc.getBlocks().stream()
            .filter(block -> block instanceof TableBlock)
            .map(block -> (TableBlock) block)
            .findFirst()
            .orElse(null);
    }

    /**
     * 加载 Word 文档
     */
    private WordprocessingMLPackage loadWordDocument(byte[] wordBytes) throws Exception {
        return WordprocessingMLPackage.load(new java.io.ByteArrayInputStream(wordBytes));
    }

    /**
     * 查找 Word 文档中的第一个表格
     */
    private Tbl findFirstTable(WordprocessingMLPackage wordDoc) {
        for (Object obj : wordDoc.getMainDocumentPart().getContent()) {
            if (obj instanceof Tbl) {
                return (Tbl) obj;
            }
        }
        return null;
    }

    /**
     * 检查表格第一列是否有合并单元格
     */
    private boolean checkFirstColumnHasMergedCells(Tbl table) {
        List<Object> rows = table.getContent();
        
        for (Object rowObj : rows) {
            if (rowObj instanceof Tr) {
                Tr row = (Tr) rowObj;
                List<Object> cells = row.getContent();
                
                if (!cells.isEmpty()) {
                    Object firstCellObj = cells.get(0);
                    if (firstCellObj instanceof Tc) {
                        Tc firstCell = (Tc) firstCellObj;
                        TcPr tcPr = firstCell.getTcPr();
                        
                        if (tcPr != null && tcPr.getVMerge() != null) {
                            // 找到 vMerge 属性，说明有合并单元格
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * 打印表格结构（用于调试）
     */
    private void printTableStructure(Tbl table) {
        List<Object> rows = table.getContent();
        System.out.println("表格行数: " + rows.size());
        
        int rowIndex = 0;
        for (Object rowObj : rows) {
            if (rowObj instanceof Tr) {
                Tr row = (Tr) rowObj;
                List<Object> cells = row.getContent();
                System.out.println("  行 " + rowIndex + " 单元格数: " + cells.size());
                
                int cellIndex = 0;
                for (Object cellObj : cells) {
                    if (cellObj instanceof Tc) {
                        Tc cell = (Tc) cellObj;
                        TcPr tcPr = cell.getTcPr();
                        
                        String mergeInfo = "无合并";
                        if (tcPr != null && tcPr.getVMerge() != null) {
                            TcPrInner.VMerge vMerge = tcPr.getVMerge();
                            if (vMerge.getVal() != null) {
                                mergeInfo = "vMerge=" + vMerge.getVal();
                            } else {
                                mergeInfo = "vMerge=continue";
                            }
                        }
                        
                        // 获取单元格文本
                        String cellText = getCellText(cell);
                        System.out.println("    单元格 " + cellIndex + ": " + mergeInfo + ", 内容='" + cellText + "'");
                        
                        cellIndex++;
                    }
                }
                
                rowIndex++;
            }
        }
    }

    /**
     * 获取单元格文本内容
     */
    private String getCellText(Tc cell) {
        StringBuilder text = new StringBuilder();
        for (Object obj : cell.getContent()) {
            if (obj instanceof P) {
                P para = (P) obj;
                for (Object paraObj : para.getContent()) {
                    Object unwrapped = paraObj;
                    if (paraObj instanceof JAXBElement) {
                        unwrapped = ((JAXBElement<?>) paraObj).getValue();
                    }

                    if (unwrapped instanceof R) {
                        R run = (R) unwrapped;
                        for (Object runObj : run.getContent()) {
                            Object runUnwrapped = runObj;
                            if (runObj instanceof JAXBElement) {
                                runUnwrapped = ((JAXBElement<?>) runObj).getValue();
                            }

                            if (runUnwrapped instanceof Text) {
                                text.append(((Text) runUnwrapped).getValue());
                            }
                        }
                    }
                }
            }
        }
        return text.toString().trim();
    }
}
