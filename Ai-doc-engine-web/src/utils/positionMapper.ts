export interface BlockPosition {
  blockId: string
  startOffset: number
  endOffset: number
  startLine: number
  endLine: number
}

export interface SelectionRange {
  startOffset: number
  endOffset: number
}

export class PositionMapper {
  private blocks: BlockPosition[] = []
  private blockIdCounter = 0

  setBlocks(blocks: any[]): void {
    this.blockIdCounter = 0
    this.blocks = blocks
      .filter((b) => b.sourceStartOffset >= 0 && b.sourceEndOffset >= 0)
      .map((b) => ({
        blockId: `block-${this.blockIdCounter++}`,
        startOffset: b.sourceStartOffset,
        endOffset: b.sourceEndOffset,
        startLine: b.sourceStartLine,
        endLine: b.sourceEndLine
      }))
      .sort((a, b) => a.startOffset - b.startOffset)
  }

  findBlockByOffset(offset: number): BlockPosition | null {
    for (const block of this.blocks) {
      if (offset >= block.startOffset && offset <= block.endOffset) {
        return block
      }
    }
    return null
  }

  findBlocksByRange(startOffset: number, endOffset: number): BlockPosition[] {
    const result: BlockPosition[] = []
    for (const block of this.blocks) {
      if (block.endOffset >= startOffset && block.startOffset <= endOffset) {
        result.push(block)
      }
    }
    return result.sort((a, b) => a.startOffset - b.startOffset)
  }

  getExactSourceRange(startOffset: number, endOffset: number): SelectionRange {
    return {
      startOffset: Math.max(0, startOffset),
      endOffset: endOffset
    }
  }

  getBlocks(): BlockPosition[] {
    return this.blocks
  }

  getBlockById(blockId: string): BlockPosition | undefined {
    return this.blocks.find((b) => b.blockId === blockId)
  }
}

export const positionMapper = new PositionMapper()
