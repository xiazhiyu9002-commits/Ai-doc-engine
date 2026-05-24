<template>
  <div class="workbench">
    <header class="workbench-header">
      <div class="header-left">
        <div class="brand">
          <div class="brand-logo">
            <img src="/logo.png" alt="AI Doc Engine Logo" class="brand-icon" />
          </div>
          <div class="brand-text">
            <h1 class="brand-name">AI Doc Engine</h1>
            <p class="brand-tagline">智能文档编辑器</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div v-if="!authStore.isAuthenticated" class="auth-buttons">
          <el-button class="auth-btn auth-btn--login" @click="showLoginDialog = true">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M10 17L15 12L10 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M15 12H3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            登录
          </el-button>
          <el-button class="auth-btn auth-btn--register" type="primary" @click="showRegisterDialog = true">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M16 21V19C16 17.9391 15.5786 16.9217 14.8284 16.1716C14.0783 15.4214 13.0609 15 12 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
              <line x1="20" y1="8" x2="20" y2="14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="23" y1="11" x2="17" y2="11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            注册
          </el-button>
        </div>
        <UserMenu v-else />
      </div>
    </header>

    <main class="workbench-main">
      <div class="workspace">
        <div class="top-toolbar">
          <button class="toolbar-btn toolbar-btn--table" @click="handleOpenTableEditor">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
              <line x1="3" y1="9" x2="21" y2="9" stroke="currentColor" stroke-width="2"/>
              <line x1="3" y1="15" x2="21" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="3" x2="9" y2="21" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="3" x2="15" y2="21" stroke="currentColor" stroke-width="2"/>
            </svg>
            <span>插入表格</span>
          </button>
          <button class="toolbar-btn toolbar-btn--formula" @click="handleOpenFormulaEditor">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 4H20L14 12L20 20H4L10 12L4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>插入公式</span>
          </button>
          <button class="toolbar-btn toolbar-btn--ocr" @click="handleOpenFormulaOcr">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
              <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
              <path d="M21 15L16 10L5 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>图片识别（缺乏算力，功能暂未开放）</span>
          </button>
        </div>
        
        <div class="panels-container">
          <div class="panel input-panel">
            <ContentInput 
              v-model="markdown" 
              @scroll="handleInputScroll" 
              @parse="handleParse" 
              @clear="handleClear"
              @selection-change="handleEditorSelectionChange"
              ref="contentInputRef" 
              :loading="documentStore.loading" 
            />
          </div>

          <div class="panel preview-panel">
            <div class="panel-header">
              <div class="panel-title">
                <svg class="panel-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <line x1="10" y1="9" x2="8" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <h3>文档预览</h3>
                <el-tag v-if="documentStore.udm" type="success" size="small" class="block-count">
                  {{ documentStore.udm.blocks.length }} 个块
                </el-tag>
              </div>
              <div class="header-controls">
                <div class="toolbar-group template-group">
                  <span class="toolbar-label">模板：</span>
                  <el-select 
                    v-model="selectedTemplateId" 
                    placeholder="选择模板" 
                    size="small" 
                    class="template-select"
                    @change="handleTemplateChange"
                    :loading="templateStore.loading"
                    popper-class="template-select-dropdown"
                    :teleported="false"
                  >
                    <el-option
                      v-for="item in templateStore.templates"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    >
                      <div class="template-option">
                        <span class="template-name">{{ item.name }}</span>
                        <small class="template-desc">{{ item.description }}</small>
                      </div>
                    </el-option>
                  </el-select>
                  <el-button 
                    class="refresh-btn"
                    link 
                    size="small" 
                    @click="templateStore.fetchTemplates()"
                  >
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </div>
                
                <div class="toolbar-divider"></div>
                
                <el-tooltip 
                  :content="syncScrollMode ? '同步滚动已开启（Ctrl+Shift+S 切换）' : '同步滚动已关闭（Ctrl+Shift+S 切换）'" 
                  placement="top"
                >
                  <div class="sync-control">
                    <svg class="sync-icon" :class="{ 'sync-active': syncScrollMode }" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M8 3V5C8 5.26522 8.10536 5.51957 8.29289 5.70711C8.48043 5.89464 8.73478 6 9 6H15C15.2652 6 15.5196 5.89464 15.7071 5.70711C15.8946 5.51957 16 5.26522 16 5V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M16 21V19C16 18.7348 15.8946 18.4804 15.7071 18.2929C15.5196 18.1054 15.2652 18 15 18H9C8.73478 18 8.48043 18.1054 8.29289 18.2929C8.10536 18.4804 8 18.7348 8 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M20 12H4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      <circle cx="4" cy="12" r="2" stroke="currentColor" stroke-width="2"/>
                      <circle cx="20" cy="12" r="2" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    <el-switch
                      v-model="syncScrollMode"
                      size="small"
                      @change="handleScrollModeChange"
                    />
                  </div>
                </el-tooltip>
                
                <div class="toolbar-divider"></div>
                
                <button class="action-btn action-btn--success" @click="handleExport" :disabled="exportLoading">
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span v-if="exportLoading">导出中...</span>
                  <span v-else>导出</span>
                </button>
              </div>
            </div>
            
            <PreviewPanel 
              @scroll="handlePreviewScroll" 
              @selection-change="handlePreviewSelectionChange"
              ref="previewPanelRef" 
            />
          </div>
        </div>
      </div>
    </main>

    <footer class="workbench-footer">
      <div class="footer-container">
        <div class="footer-main">
          <div class="footer-brand">
            <div class="footer-logo">
              <img src="/logo.png" alt="AI Doc Engine" class="footer-logo-img" />
              <div class="footer-brand-text">
                <span class="footer-brand-name">AI Doc Engine</span>
                <span class="footer-brand-desc">智能文档编辑器</span>
              </div>
            </div>
            <p class="footer-intro">基于人工智能技术的智能文档编辑与转换平台，为您提供高效、专业的文档处理服务。</p>
          </div>
          
          <div class="footer-links">
            <div class="footer-column">
              <h5 class="column-title">联系方式</h5>
              <ul class="column-list">
                <li>
                  <svg class="footer-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M3 8L10.8906 13.2604C11.5624 13.7083 12.4376 13.7083 13.1094 13.2604L21 8M5 19H19C20.1046 19 21 18.1046 21 17V7C21 5.89543 20.1046 5 19 5H5C3.89543 5 3 5.89543 3 7V17C3 18.1046 3.89543 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <a href="mailto:aidoc@gznc.edu.cn">aidoc@gznc.edu.cn</a>
                </li>
                <li>
                  <svg class="footer-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 16.92V19.92C22.0011 20.1985 21.9441 20.4742 21.8325 20.7293C21.7209 20.9845 21.5573 21.2136 21.3521 21.4019C21.1468 21.5901 20.9046 21.7335 20.6407 21.8227C20.3769 21.9119 20.0974 21.9451 19.82 21.92C16.7428 21.5856 13.787 20.5341 11.19 18.85C8.77382 17.3147 6.72533 15.2662 5.18999 12.85C3.49997 10.2412 2.44824 7.27099 2.11999 4.18C2.09494 3.90347 2.12781 3.62476 2.21643 3.36162C2.30506 3.09849 2.4475 2.85669 2.6347 2.65162C2.82189 2.44655 3.04974 2.28271 3.30372 2.17052C3.55771 2.05833 3.83227 2.00026 4.10999 2H7.10999C7.5953 1.99522 8.06579 2.16708 8.43376 2.48353C8.80173 2.79999 9.04207 3.23945 9.10999 3.72C9.23662 4.68007 9.47144 5.62273 9.80999 6.53C9.94454 6.88792 9.97366 7.27691 9.8939 7.65088C9.81415 8.02485 9.62886 8.36811 9.35999 8.64L8.08999 9.91C9.51355 12.4135 11.5864 14.4864 14.09 15.91L15.36 14.64C15.6319 14.3711 15.9751 14.1858 16.3491 14.1061C16.7231 14.0263 17.1121 14.0554 17.47 14.19C18.3773 14.5286 19.3199 14.7634 20.28 14.89C20.7658 14.9585 21.2094 15.2032 21.5265 15.5775C21.8437 15.9518 22.0122 16.4296 22 16.92Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>0851-XXXX-XXXX</span>
                </li>
                <li>
                  <svg class="footer-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <span>贵州省贵阳市乌当区高新路115号</span>
                </li>
              </ul>
            </div>
            
            <div class="footer-column">
              <h5 class="column-title">快速链接</h5>
              <ul class="column-list">
                <li><a href="https://www.gznc.edu.cn" target="_blank" rel="noopener">贵州师范学院官网</a></li>
                <li><a href="https://math.gznc.edu.cn" target="_blank" rel="noopener">数学与大数据学院</a></li>
                <li><a href="https://ailab.gznc.edu.cn" target="_blank" rel="noopener">人工智能重点实验室</a></li>
              </ul>
            </div>
            
            <div class="footer-column">
              <h5 class="column-title">关注我们</h5>
              <div class="social-links">
                <a href="https://github.com" target="_blank" rel="noopener" class="social-link" title="GitHub">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                  </svg>
                </a>
                <a href="https://weixin.qq.com" target="_blank" rel="noopener" class="social-link" title="微信公众号">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                  </svg>
                </a>
              </div>
            </div>
          </div>
        </div>
        
        <div class="footer-partners">
          <h5 class="partners-title">合作单位</h5>
          <div class="partners-list">
            <span class="partner-item">贵州师范学院数学与大数据学院</span>
            <span class="partner-divider">|</span>
            <span class="partner-item">贵州省人工智能与内脑计算全省重点实验室</span>
            <span class="partner-divider">|</span>
            <span class="partner-item">贵州师范学院教育网络中心</span>
            <span class="partner-divider">|</span>
            <span class="partner-item">贵州师范学院教务处</span>
          </div>
        </div>
        
        <div class="footer-bottom">
          <p class="copyright">© 2024 AI Doc Engine. All rights reserved.</p>
          <p class="icp">黔ICP备XXXXXXXX号-1</p>
        </div>
      </div>
    </footer>

    <el-dialog v-model="showTemplateDialog" title="选择模板" width="600px" class="custom-dialog">
      <TemplatePanel @select="showTemplateDialog = false" />
    </el-dialog>

    <el-dialog v-model="showFormulaEditor" title="插入公式" width="700px" class="custom-dialog">
      <div class="dialog-alert">
        <svg class="alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>支持 LaTeX 语法，例如：$E=mc^2$、$\frac{a}{b}$、$\sqrt{x}$ 等</span>
      </div>
      <FormulaLatexEditor v-model="formulaLatex" />
      <FormulaPreview :latex="formulaLatex" />
      <template #footer>
        <div class="dialog-footer">
          <el-button class="dialog-btn" @click="showFormulaEditor = false">取消</el-button>
          <el-button class="dialog-btn dialog-btn--primary" type="primary" @click="handleInsertFormula">插入</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="showExportDialog" title="导出文档" width="450px">
      <div class="export-content">
        <div class="export-notice">
          <svg class="notice-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="#409eff" stroke-width="2"/>
            <path d="M12 8v4M12 16h.01" stroke="#409eff" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="notice-content">
            <div class="notice-title">导出的 Word 文档特性：</div>
            <ul class="notice-list">
              <li>所有内容均为可编辑格式</li>
              <li>公式为 Word 原生公式（OMML），可直接编辑</li>
              <li>流程图为 Word 图形对象（DrawingML），可编辑</li>
              <li>表格为原生表格，可编辑</li>
            </ul>
          </div>
        </div>
        <div class="export-input">
          <label>文件名（不需要输入.docx后缀）</label>
          <el-input 
            v-model="exportFileName" 
            placeholder="请输入文件名，留空则自动生成"
            clearable
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="handleConfirmExport">确定导出</el-button>
          <el-button @click="showExportDialog = false">取消</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showFormulaOcr" title="识别公式" width="800px" class="custom-dialog">
      <div class="dialog-alert">
        <svg class="alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <div class="alert-content">
          <div class="alert-title">识别公式说明</div>
          <div class="alert-text">上传公式截图进行识别，确认后点击"插入文档"，公式将添加到鼠标所在位置。</div>
        </div>
      </div>
      <FormulaImageUpload ref="formulaImageUploadRef" @success="handleOcrSuccess" />
      <FormulaOcrResultPanel
        v-if="ocrResult"
        :result="ocrResult"
        @insert="handleInsertOcrFormula"
      />
    </el-dialog>

    <el-dialog v-model="showTableEditor" title="插入表格" width="900px" class="custom-dialog">
      <div class="dialog-alert">
        <svg class="alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <div class="alert-content">
          <div class="alert-title">插入表格说明</div>
          <div class="alert-text">编辑完成后点击"插入表格"按钮，表格将添加到鼠标所在位置。</div>
        </div>
      </div>
      <TableEditor :key="tableEditorKey" @insert="handleInsertTable" />
    </el-dialog>

    <el-dialog v-model="showLoginDialog" title="登录" width="450px" :close-on-click-modal="false" class="custom-dialog">
      <LoginForm @success="handleAuthSuccess" @switch-to-register="switchToRegister" @switch-to-forgot="switchToForgot" />
    </el-dialog>

    <el-dialog v-model="showRegisterDialog" title="注册" width="450px" :close-on-click-modal="false" class="custom-dialog">
      <RegisterForm @success="handleAuthSuccess" @switch-to-login="switchToLogin" />
    </el-dialog>

    <el-dialog v-model="showForgotDialog" title="忘记密码" width="450px" :close-on-click-modal="false" class="custom-dialog">
      <ForgotPasswordForm @success="handleForgotSuccess" @switch-to-login="switchToLogin" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  Refresh
} from '@element-plus/icons-vue'
import { useDocumentStore } from '@/store/document'
import { useAuthStore } from '@/store/auth'
import { useTemplateStore } from '@/store/template'
import ContentInput from '@/components/ContentInput.vue'
import PreviewPanel from '@/components/PreviewPanel.vue'
import TemplatePanel from '@/components/TemplatePanel.vue'
import UserMenu from '@/components/auth/UserMenu.vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import RegisterForm from '@/components/auth/RegisterForm.vue'
import ForgotPasswordForm from '@/components/auth/ForgotPasswordForm.vue'
import FormulaLatexEditor from '@/components/formula/FormulaLatexEditor.vue'
import FormulaPreview from '@/components/formula/FormulaPreview.vue'
import FormulaImageUpload from '@/components/formula/FormulaImageUpload.vue'
import FormulaOcrResultPanel from '@/components/formula/FormulaOcrResultPanel.vue'
import TableEditor from '@/components/TableEditor.vue'
import type { FormulaOcrResponse } from '@/types/formula'
import type { TableBlock } from '@/types/document'
import type { ScrollData } from '@/utils/scroll'
import { debugLog } from '@/utils/scroll'
import { createScrollSyncManager, type SmoothScrollSyncManager } from '@/utils/smoothScrollSync'
import { scrollDebugger } from '@/utils/scrollSyncDebugger'
import { formatMathFormulas } from '@/utils/markdownFormatter'
import { positionMapper } from '@/utils/positionMapper'

const documentStore = useDocumentStore()
const authStore = useAuthStore()
const templateStore = useTemplateStore()

const markdown = ref('')
const exportLoading = ref(false)
const selectedTemplateId = ref<number | undefined>(undefined)

const syncScrollMode = ref(true)

const isSelectionSyncing = ref(false)

let scrollSyncManager: SmoothScrollSyncManager | null = null

onMounted(async () => {
  await templateStore.fetchTemplates()
  if (templateStore.selectedTemplate) {
    selectedTemplateId.value = templateStore.selectedTemplate.id
  }
  
  if (authStore.isAuthenticated) {
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      console.error('Failed to fetch user info:', error)
    }
  }
  
  await nextTick()
  initScrollSync()
  
  window.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyDown)
  
  // 清理滚动同步管理器
  if (scrollSyncManager) {
    scrollSyncManager.destroy()
    scrollSyncManager = null
  }
})

const handleKeyDown = (e: KeyboardEvent) => {
  if (e.ctrlKey && e.shiftKey && e.key === 'S') {
    e.preventDefault()
    syncScrollMode.value = !syncScrollMode.value
    handleScrollModeChange(syncScrollMode.value)
  }
}

const handleScrollModeChange = async (enabled: boolean) => {
  if (enabled) {
    debugLog('切换到同步模式')
    
    await nextTick()
    initScrollSync()
    
    ElMessage.success('同步滚动已开启')
  } else {
    if (scrollSyncManager) {
      scrollSyncManager.destroy()
      scrollSyncManager = null
    }
    ElMessage.info('同步滚动已关闭，可独立滚动')
  }
}

const handleTemplateChange = (id: number) => {
  const template = templateStore.templates.find(t => t.id === id)
  if (template) {
    templateStore.selectTemplate(template)
  }
}

const contentInputRef = ref<InstanceType<typeof ContentInput> | null>(null)
const previewPanelRef = ref<InstanceType<typeof PreviewPanel> | null>(null)

/**
 * 初始化滚动同步
 */
const initScrollSync = () => {
  if (!contentInputRef.value || !previewPanelRef.value) {
    debugLog('初始化滚动同步失败: 组件引用为空')
    return
  }

  const editorElement = contentInputRef.value.getScrollContainer()
  const previewElement = previewPanelRef.value.getScrollContainer()

  if (!editorElement || !previewElement) {
    debugLog('初始化滚动同步失败: DOM元素为空')
    return
  }

  // 创建滚动同步管理器
  scrollSyncManager = createScrollSyncManager({
    smoothness: 0.15,       // 平滑度
    debounceDelay: 30,      // 防抖延迟
    enableSmartOffset: false // 禁用智能偏移，使用纯百分比映射
  })

  scrollSyncManager.init(editorElement, previewElement)
  
  // 连接调试器（开发环境）
  if (import.meta.env.DEV) {
    scrollDebugger.setManager(scrollSyncManager)
  }
  
  debugLog('滚动同步已初始化')
}

/**
 * 处理编辑器滚动
 */
const handleInputScroll = (scrollData: ScrollData) => {
  if (!syncScrollMode.value || !scrollSyncManager) {
    return
  }

  scrollSyncManager.handleEditorScroll(
    scrollData.scrollTop,
    scrollData.scrollHeight,
    scrollData.clientHeight
  )
}

/**
 * 处理预览滚动
 */
const handlePreviewScroll = (scrollData: ScrollData) => {
  if (!syncScrollMode.value || !scrollSyncManager) {
    return
  }

  scrollSyncManager.handlePreviewScroll(
    scrollData.scrollTop,
    scrollData.scrollHeight,
    scrollData.clientHeight
  )
}

const showTemplateDialog = ref(false)
const showFormulaEditor = ref(false)
const showFormulaOcr = ref(false)
const showTableEditor = ref(false)
const showLoginDialog = ref(false)
const showRegisterDialog = ref(false)
const showForgotDialog = ref(false)
const showExportDialog = ref(false)
const exportFileName = ref('')

// 用于强制重新创建组件的 key
const tableEditorKey = ref(0)

const formulaLatex = ref('')
const ocrResult = ref<FormulaOcrResponse | null>(null)
const formulaImageUploadRef = ref<InstanceType<typeof FormulaImageUpload> | null>(null)

/**
 * 打开公式编辑器
 */
const handleOpenFormulaEditor = () => {
  formulaLatex.value = '' // 清空上一次的公式
  showFormulaEditor.value = true
}

/**
 * 打开公式识别
 */
const handleOpenFormulaOcr = () => {
  // 检查登录状态
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再使用图片识别功能')
    showLoginDialog.value = true
    return
  }
  
  ocrResult.value = null
  formulaImageUploadRef.value?.reset()
  showFormulaOcr.value = true
}

/**
 * 打开表格编辑器
 */
const handleOpenTableEditor = () => {
  tableEditorKey.value++ // 强制重新创建组件，清空上一次的数据
  showTableEditor.value = true
}

const handleParse = async () => {
  if (!markdown.value.trim()) {
    ElMessage.warning('请输入 Markdown 内容')
    return
  }
  
  try {
    const formatted = formatMathFormulas(markdown.value)
    markdown.value = formatted
    await documentStore.parseMarkdown(formatted)
    
    if (documentStore.udm?.blocks) {
      positionMapper.setBlocks(documentStore.udm.blocks)
    }
    
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 200))
    
    if (syncScrollMode.value && scrollSyncManager) {
      scrollSyncManager.refresh()
    }
    
    ElMessage.success('解析成功')
  } catch (error: any) {
    ElMessage.error(error.message || '解析失败')
  }
}

const handleExport = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再导出文档')
    showLoginDialog.value = true
    return
  }
  
  if (!markdown.value.trim()) {
    ElMessage.warning('请先输入文档内容')
    return
  }
  
  exportFileName.value = ''
  showExportDialog.value = true
}

const handleConfirmExport = async () => {
  if (exportFileName.value && /[\\/:*?"<>|]/.test(exportFileName.value)) {
    ElMessage.warning('文件名不能包含 \\ / : * ? " < > | 等特殊字符')
    return
  }
  
  showExportDialog.value = false
  exportLoading.value = true
  
  const loadingInstance = ElMessage({
    message: '正在生成 Word 文档，请稍候...',
    type: 'info',
    duration: 0,
    icon: Download
  })
  
  try {
    const result = await documentStore.exportWordDirect(selectedTemplateId.value, exportFileName.value || undefined)
    
    loadingInstance.close()
    
    const link = document.createElement('a')
    link.href = result.url
    link.download = result.filename
    link.click()
    
    setTimeout(() => {
      window.URL.revokeObjectURL(result.url)
    }, 1000)
    
    ElMessage.success('导出成功！文档中所有内容均可编辑')
  } catch (error: any) {
    if (error === 'cancel') return
    loadingInstance?.close()
    ElMessage.error(error.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleInsertFormula = () => {
  if (!formulaLatex.value.trim()) {
    ElMessage.warning('请输入公式')
    return
  }
  
  const text = `$$${formulaLatex.value}$$`
  contentInputRef.value?.insertBlockAtCursor(text)
  
  showFormulaEditor.value = false
  formulaLatex.value = ''
  ElMessage.success('公式已插入')
}

const handleOcrSuccess = (result: FormulaOcrResponse) => {
  ocrResult.value = result
}

const handleInsertOcrFormula = (latex: string) => {
  // 根据内容类型决定插入格式
  const contentType = ocrResult.value?.contentType || 'formula'
  
  let text = ''
  if (contentType === 'flowchart') {
    // 流程图格式
    text = '\n```mermaid\n' + latex + '\n```\n'
  } else if (contentType === 'table') {
    // 表格格式（如果OCR识别出表格）
    text = '\n' + latex + '\n'
  } else {
    // 默认公式格式
    text = `$$${latex}$$`
  }
  
  contentInputRef.value?.insertBlockAtCursor(text)
  
  // 清理状态
  showFormulaOcr.value = false
  ocrResult.value = null
  
  const typeText = contentType === 'flowchart' ? '流程图' : contentType === 'table' ? '表格' : '公式'
  ElMessage.success(`${typeText}已插入`)
}

const handleInsertTable = (table: TableBlock) => {
  let markdownTable = '\n'
  if (table.content.headers && table.content.headers.length > 0) {
    markdownTable += '| ' + table.content.headers.join(' | ') + ' |\n'
    markdownTable += '| ' + table.content.headers.map(() => '---').join(' | ') + ' |\n'
  }
  if (table.content.rows && table.content.rows.length > 0) {
    table.content.rows.forEach(row => {
      markdownTable += '| ' + row.join(' | ') + ' |\n'
    })
  }
  markdownTable += '\n'
  
  contentInputRef.value?.insertBlockAtCursor(markdownTable)
  showTableEditor.value = false
  ElMessage.success('表格已插入')
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空文档吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'reverse-buttons-dialog'
    })
    
    markdown.value = ''
    documentStore.clearDocument()
    ElMessage.success('文档已清空')
  } catch (error) {
  }
}

const handleAuthSuccess = () => {
  showLoginDialog.value = false
  showRegisterDialog.value = false
  ElMessage.success('登录成功')
}

const handleForgotSuccess = () => {
  showForgotDialog.value = false
  ElMessage.success('重置链接已发送到您的邮箱')
}

const switchToRegister = () => {
  showLoginDialog.value = false
  showRegisterDialog.value = true
}

const switchToLogin = () => {
  showRegisterDialog.value = false
  showForgotDialog.value = false
  showLoginDialog.value = true
}

const switchToForgot = () => {
  showLoginDialog.value = false
  showForgotDialog.value = true
}

const handleEditorSelectionChange = (range: { startOffset: number; endOffset: number } | null) => {
  if (isSelectionSyncing.value) return
  
  if (!range) {
    previewPanelRef.value?.clearHighlight()
    return
  }
  
  isSelectionSyncing.value = true
  
  previewPanelRef.value?.highlightBySourceRange(range.startOffset, range.endOffset)
  
  setTimeout(() => {
    isSelectionSyncing.value = false
  }, 100)
}

const handlePreviewSelectionChange = (range: { startOffset: number; endOffset: number } | null) => {
  if (isSelectionSyncing.value) return
  
  if (!range) {
    return
  }
  
  isSelectionSyncing.value = true
  
  contentInputRef.value?.setSelection(range.startOffset, range.endOffset)
  
  setTimeout(() => {
    isSelectionSyncing.value = false
  }, 100)
}
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

.workbench-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px 0 24px;
  height: 64px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: 25px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  /* background: linear-gradient(135deg, rgb(3, 77, 226) 0%, #6904d4 100%); */
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.brand-icon {
  width: 45px;
  height: 45px;
  color: white;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.3px;
  color: #111827;
  line-height: 1.2;
}

.brand-tagline {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.auth-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  padding: 0 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.auth-btn--login {
  background: white;
  border: 1px solid #e5e7eb;
  color: #374151;
}

.auth-btn--login:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
}

.auth-btn--register {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.auth-btn--register:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.workbench-main {
  flex: none;
  height: calc(100vh - 64px);
  overflow: hidden;
  padding: 16px;
  margin-top: 64px;
}

.workbench-footer {
  flex-shrink: 0;
  background: linear-gradient(to bottom, #f8fafc, #f1f5f9);
  border-top: 1px solid #e5e7eb;
  padding: 32px 24px 20px;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.05);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-main {
  display: flex;
  justify-content: space-between;
  gap: 48px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.footer-brand {
  flex: 0 0 280px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.footer-logo-img {
  width: 40px;
  height: 40px;
}

.footer-brand-text {
  display: flex;
  flex-direction: column;
}

.footer-brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.footer-brand-desc {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.footer-intro {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.7;
  margin: 0;
}

.footer-links {
  display: flex;
  gap: 48px;
  flex: 1;
}

.footer-column {
  flex: 1;
}

.column-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 16px 0;
  letter-spacing: 0.5px;
}

.column-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.column-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #6b7280;
}

.column-list li:last-child {
  margin-bottom: 0;
}

.column-list a {
  color: #6b7280;
  text-decoration: none;
  transition: color 0.2s ease;
}

.column-list a:hover {
  color: #667eea;
}

.footer-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: #9ca3af;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f3f4f6;
  color: #6b7280;
  transition: all 0.2s ease;
}

.social-link:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.social-link svg {
  width: 18px;
  height: 18px;
}

.footer-partners {
  padding: 20px 0;
  border-bottom: 1px solid #e5e7eb;
}

.partners-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px 0;
  text-align: center;
  letter-spacing: 0.5px;
}

.partners-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.partner-item {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.partner-divider {
  color: #d1d5db;
  font-size: 12px;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
}

.copyright,
.icp {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
}

.workspace {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.top-toolbar {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding: 10px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  align-items: center;
}

.toolbar-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s ease;
  background: white;
  color: #374151;
}

.toolbar-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.toolbar-btn .btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.panels-container {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
  align-items: stretch;
}

.panel {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: box-shadow 0.2s ease;
  height: 100%;
}

.panel:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.06);
}

.input-panel {
  flex: 1;
  min-width: 400px;
  background: #fefefe;
}

.preview-panel {
  flex: 1;
  min-width: 400px;
  background: #f8fafc;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(to bottom, #f8fafc, #f1f5f9);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-icon {
  width: 20px;
  height: 20px;
  color: #667eea;
}

.panel-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.block-count {
  font-size: 12px;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.template-group {
  position: relative;
}

.template-select {
  width: 140px;
}

.template-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb;
}

.template-select :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db;
}

.template-select :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2), 0 0 0 1px #667eea;
}

/* 固定下拉菜单位置和宽度 */
.template-select :deep(.el-select__popper) {
  width: 140px !important;
  position: absolute !important;
  transform: none !important;
  top: 100% !important;
  left: 0 !important;
  margin-top: 4px !important;
}

.template-select-dropdown {
  width: 140px !important;
}

.template-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.template-name {
  font-size: 14px;
  color: #111827;
}

.template-desc {
  font-size: 12px;
  color: #9ca3af;
}

.refresh-btn {
  padding: 6px;
  color: #6b7280;
}

.refresh-btn:hover {
  color: #667eea;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
}

.sync-control {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.sync-icon {
  width: 18px;
  height: 18px;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.sync-icon.sync-active {
  color: #667eea;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s ease;
  background: white;
  color: #374151;
}

.action-btn .btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.action-btn--success {
  background: #10b981;
  color: white;
  border-color: #10b981;
}

.action-btn--success:hover {
  background: #059669;
  border-color: #059669;
}

.action-btn--success:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.custom-dialog :deep(.el-dialog__header) {
  text-align: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.custom-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.custom-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f3f4f6;
}

.dialog-alert {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  margin-bottom: 20px;
}

.alert-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-top: 2px;
  color: #3b82f6;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.alert-text {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.export-content {
  padding: 10px 0;
}

.export-notice {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.notice-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  margin-top: 2px;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.notice-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.export-input label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.dialog-btn {
  height: 40px;
  padding: 0 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
}

.dialog-btn--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.dialog-btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

@media (max-width: 1024px) {
  .panels-container {
    flex-direction: column;
  }
  
  .panel {
    min-width: 100%;
  }
  
  .input-panel,
  .preview-panel {
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .workbench-header {
    padding: 0 16px;
    height: 56px;
  }
  
  .brand-tagline {
    display: none;
  }
  
  .workbench-main {
    padding: 12px;
  }
  
  .workbench-footer {
    padding: 24px 16px 16px;
  }
  
  .footer-main {
    flex-direction: column;
    gap: 24px;
  }
  
  .footer-brand {
    flex: none;
    text-align: center;
  }
  
  .footer-logo {
    justify-content: center;
  }
  
  .footer-links {
    flex-direction: column;
    gap: 20px;
  }
  
  .footer-column {
    text-align: center;
  }
  
  .column-list li {
    justify-content: center;
  }
  
  .social-links {
    justify-content: center;
  }
  
  .partners-list {
    flex-direction: column;
    gap: 6px;
  }
  
  .partner-divider {
    display: none;
  }
  
  .partner-item {
    font-size: 12px;
  }
  
  .footer-bottom {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
  
  .workspace {
    gap: 12px;
  }
  
  .top-toolbar {
    flex-wrap: wrap;
    padding: 8px 12px;
  }
  
  .panels-container {
    gap: 12px;
  }
  
  .panel-header {
    flex-wrap: wrap;
    padding: 10px 12px;
  }
  
  .header-controls {
    flex-wrap: wrap;
    width: 100%;
    justify-content: flex-end;
    margin-top: 8px;
  }
  
  .template-select {
    width: 120px;
  }
  
  .toolbar-divider {
    display: none;
  }
}

@media (max-width: 480px) {
  .workbench-header {
    padding: 0 12px;
  }
  
  .brand-logo {
    width: 36px;
    height: 36px;
  }
  
  .brand-icon {
    width: 20px;
    height: 20px;
  }
  
  .brand-name {
    font-size: 18px;
  }
  
  .auth-btn {
    padding: 0 16px;
    font-size: 13px;
  }
  
  .panel-header {
    padding: 12px 16px;
  }
  
  .panel-title h3 {
    font-size: 15px;
  }
}
</style>

<style>
.reverse-buttons-dialog .el-message-box__btns {
  flex-direction: row-reverse;
  justify-content: flex-start;
}
</style>
