import React, { useState, useRef, useEffect } from 'react';
import {
  Upload,
  FileSpreadsheet,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Download,
  Trash2,
  Eye,
  Calendar,
  User,
  FileText,
} from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useLanguage } from '../context/LanguageContext';
import { useToast } from '../components/Toast';
import Loader from '../components/Loader';
import Modal, { ModalFooter } from '../components/Modal';
import { importExcel } from '../api/reports';

export default function ImportExcel() {
  const { t } = useLanguage();
  const toast = useToast();
  const fileInputRef = useRef(null);

  const [file, setFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [previewModal, setPreviewModal] = useState(null);

  // Charger l'historique depuis le localStorage
  useEffect(() => {
    const saved = localStorage.getItem('importHistory');
    if (saved) {
      try {
        setHistory(JSON.parse(saved));
      } catch (e) {
        console.error('Failed to parse import history', e);
      }
    }
  }, []);

  const saveToHistory = (record) => {
    const updated = [record, ...history].slice(0, 50); // Garder les 50 derniers
    setHistory(updated);
    localStorage.setItem('importHistory', JSON.stringify(updated));
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const droppedFile = e.dataTransfer.files?.[0];
    if (droppedFile) {
      validateAndSetFile(droppedFile);
    }
  };

  const handleFileInput = (e) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      validateAndSetFile(selectedFile);
    }
  };

  const validateAndSetFile = (selectedFile) => {
    // Validation du format
    if (!selectedFile.name.endsWith('.xlsx') && !selectedFile.name.endsWith('.xls')) {
      toast.error(t('import.uploadError') + ' - Format invalide');
      return;
    }

    // Validation de la taille (max 10MB)
    if (selectedFile.size > 10 * 1024 * 1024) {
      toast.error(t('import.uploadError') + ' - Fichier trop volumineux (max 10 MB)');
      return;
    }

    setFile(selectedFile);
    setImportResult(null);
  };

  const handleImport = async () => {
    if (!file) return;

    setImporting(true);
    const startTime = Date.now();

    try {
      const result = await importExcel(file);
      const duration = ((Date.now() - startTime) / 1000).toFixed(2);

      const historyRecord = {
        id: Date.now(),
        filename: file.name,
        timestamp: new Date().toISOString(),
        recordsImported: result.recordsImported || 0,
        errors: result.errors || [],
        status: result.errors?.length > 0 ? 'warning' : 'success',
        duration,
      };

      saveToHistory(historyRecord);
      setImportResult(historyRecord);

      if (result.errors?.length > 0) {
        toast.warning(
          `${t('import.uploadSuccess')} - ${result.errors.length} ${t('import.errors')}. ${result.recordsImported} ${t('import.recordsImported')}.`
        );
      } else {
        toast.success(`${t('import.uploadSuccess')} ${result.recordsImported} ${t('import.recordsImported')} en ${duration}s.`);
      }

      setFile(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    } catch (err) {
      const errorMsg = err.response?.data?.error || 'Erreur lors de l\'import du fichier.';
      
      const historyRecord = {
        id: Date.now(),
        filename: file.name,
        timestamp: new Date().toISOString(),
        recordsImported: 0,
        errors: [errorMsg],
        status: 'error',
        duration: ((Date.now() - startTime) / 1000).toFixed(2),
      };

      saveToHistory(historyRecord);
      setImportResult(historyRecord);
      toast.error(errorMsg);
    } finally {
      setImporting(false);
    }
  };

  const clearHistory = () => {
    if (confirm(t('import.deleteConfirm') || 'Confirmer la suppression ?')) {
      setHistory([]);
      localStorage.removeItem('importHistory');
      toast.info(t('import.historyCleared') || 'Historique effacé');
    }
  };

  return (
    <Layout>
      <PageHeader
        eyebrow={t('import.eyebrow')}
        title={t('import.title')}
        action={
          history.length > 0 && (
            <button onClick={clearHistory} className="btn-secondary">
              <Trash2 size={16} />
              {t('import.clearHistory') || 'Effacer l\'historique'}
            </button>
          )
        }
      />

      <div className="p-8 space-y-6">
        {/* Zone d'upload */}
        <div className="panel p-6">
          <div className="eyebrow mb-4">{t('import.uploadTitle')}</div>

          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-xl py-12 flex flex-col items-center justify-center gap-4 cursor-pointer transition-all ${
              dragActive
                ? 'border-blue-500 bg-blue-50'
                : file
                ? 'border-green-500 bg-green-50'
                : 'border-border hover:border-blue-400 hover:bg-blue-50/30'
            }`}
          >
            {file ? (
              <>
                <div className="h-16 w-16 rounded-full bg-green-100 flex items-center justify-center">
                  <FileSpreadsheet size={32} className="text-green-600" />
                </div>
                <div className="text-center">
                  <p className="font-medium text-text">{file.name}</p>
                  <p className="text-sm text-text-muted">
                    {(file.size / 1024).toFixed(2)} KB
                  </p>
                </div>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setFile(null);
                    if (fileInputRef.current) fileInputRef.current.value = '';
                  }}
                  className="text-sm text-text-muted hover:text-alert underline"
                >
                  {t('import.changeFile')}
                </button>
              </>
            ) : (
              <>
                <div className="h-16 w-16 rounded-full bg-blue-100 flex items-center justify-center">
                  <Upload size={32} className="text-blue-600" />
                </div>
                <div className="text-center">
                  <p className="font-medium text-text">
                    {t('import.dragDrop')}
                  </p>
                  <p className="text-sm text-text-muted">
                    {t('import.or')} (.xlsx, .xls)
                  </p>
                  <p className="text-xs text-text-faint mt-2">{t('import.maxSize')}</p>
                </div>
              </>
            )}

            <input
              ref={fileInputRef}
              type="file"
              accept=".xlsx,.xls"
              className="hidden"
              onChange={handleFileInput}
            />
          </div>

          {file && (
            <div className="mt-4 flex gap-3">
              <button
                onClick={handleImport}
                disabled={importing}
                className="btn-primary flex-1"
              >
                {importing ? t('import.importing') : t('import.upload')}
              </button>
            </div>
          )}

          {/* Résultat de l'import */}
          {importResult && (
            <div
              className={`mt-4 p-4 rounded-lg border ${
                importResult.status === 'success'
                  ? 'bg-green-50 border-green-200'
                  : importResult.status === 'warning'
                  ? 'bg-yellow-50 border-yellow-200'
                  : 'bg-red-50 border-red-200'
              }`}
            >
              <div className="flex items-start gap-3">
                {importResult.status === 'success' && (
                  <CheckCircle2 size={20} className="text-green-600 shrink-0 mt-0.5" />
                )}
                {importResult.status === 'warning' && (
                  <AlertTriangle size={20} className="text-yellow-600 shrink-0 mt-0.5" />
                )}
                {importResult.status === 'error' && (
                  <XCircle size={20} className="text-red-600 shrink-0 mt-0.5" />
                )}

                <div className="flex-1">
                  <p className="font-medium text-sm mb-1">
                    {importResult.status === 'success'
                      ? 'Import réussi'
                      : importResult.status === 'warning'
                      ? 'Import terminé avec des avertissements'
                      : 'Échec de l\'import'}
                  </p>
                  <p className="text-sm text-text-muted">
                    {importResult.recordsImported} ligne(s) importée(s) en{' '}
                    {importResult.duration}s
                  </p>
                  {importResult.errors?.length > 0 && (
                    <div className="mt-2 space-y-1">
                      {importResult.errors.slice(0, 5).map((err, i) => (
                        <p key={i} className="text-xs text-text-muted">
                          • {err}
                        </p>
                      ))}
                      {importResult.errors.length > 5 && (
                        <p className="text-xs text-text-faint">
                          ... et {importResult.errors.length - 5} autre(s) erreur(s)
                        </p>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Instructions */}
        <div className="panel p-6">
          <div className="eyebrow mb-3">{t('import.formatGuideTitle')}</div>
          <div className="space-y-2 text-sm text-text-muted">
            <p>
              {t('import.formatGuideIntro')}
            </p>
            <ul className="list-disc list-inside space-y-1 ml-2">
              <li>
                <span className="font-mono font-semibold text-text">date</span> : {t('import.dateColumn')}
              </li>
              <li>
                <span className="font-mono font-semibold text-text">line</span> : {t('import.lineColumn')}
              </li>
              <li>
                <span className="font-mono font-semibold text-text">quantity</span> : {t('import.quantityColumn')}
              </li>
              <li>
                <span className="font-mono font-semibold text-text">defects</span> : {t('import.defectsColumn')}
              </li>
            </ul>
            <p className="mt-3 text-xs text-text-faint">
              {t('import.invalidRowsNote')}
            </p>
          </div>
        </div>

        {/* Historique des imports */}
        {history.length > 0 && (
          <div className="panel">
            <div className="p-5 border-b border-border">
              <div className="eyebrow">{t('import.history')}</div>
            </div>
            <div className="overflow-x-auto">
              <table className="table-industrial w-full">
                <thead>
                  <tr>
                    <th>{t('import.status')}</th>
                    <th>{t('import.file')}</th>
                    <th>{t('import.dateTime')}</th>
                    <th>{t('import.linesImported')}</th>
                    <th>{t('import.errors')}</th>
                    <th>{t('import.duration')}</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((record) => (
                    <tr key={record.id}>
                      <td>
                        <div className="flex items-center gap-2">
                          {record.status === 'success' && (
                            <CheckCircle2 size={16} className="text-green-600" />
                          )}
                          {record.status === 'warning' && (
                            <AlertTriangle size={16} className="text-yellow-600" />
                          )}
                          {record.status === 'error' && (
                            <XCircle size={16} className="text-red-600" />
                          )}
                        </div>
                      </td>
                      <td>
                        <div className="flex items-center gap-2">
                          <FileSpreadsheet size={14} className="text-text-muted" />
                          <span className="font-mono text-xs">{record.filename}</span>
                        </div>
                      </td>
                      <td className="font-mono text-xs text-text-muted">
                        {formatDateTime(record.timestamp)}
                      </td>
                      <td className="font-mono text-teal font-semibold">
                        {record.recordsImported}
                      </td>
                      <td className="font-mono text-xs">
                        {record.errors?.length > 0 ? (
                          <span className="text-alert">{record.errors.length}</span>
                        ) : (
                          <span className="text-text-faint">0</span>
                        )}
                      </td>
                      <td className="font-mono text-xs text-text-muted">
                        {record.duration}s
                      </td>
                      <td>
                        {record.errors?.length > 0 && (
                          <button
                            onClick={() => setPreviewModal(record)}
                            className="p-2 rounded-lg hover:bg-surface-3 hover:text-amber text-text-muted transition-colors"
                            title={t('import.viewErrors')}
                          >
                            <Eye size={15} />
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {/* Modal de prévisualisation des erreurs */}
      <Modal
        isOpen={!!previewModal}
        onClose={() => setPreviewModal(null)}
        title={t('import.importDetails')}
        size="md"
      >
        {previewModal && (
          <div className="p-6 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <div className="text-xs text-text-faint mb-1">{t('import.file')}</div>
                <div className="font-mono text-sm">{previewModal.filename}</div>
              </div>
              <div>
                <div className="text-xs text-text-faint mb-1">{t('import.date')}</div>
                <div className="font-mono text-sm">
                  {formatDateTime(previewModal.timestamp)}
                </div>
              </div>
              <div>
                <div className="text-xs text-text-faint mb-1">{t('import.linesImported')}</div>
                <div className="font-mono text-sm text-teal font-semibold">
                  {previewModal.recordsImported}
                </div>
              </div>
              <div>
                <div className="text-xs text-text-faint mb-1">{t('import.errors')}</div>
                <div className="font-mono text-sm text-alert font-semibold">
                  {previewModal.errors?.length || 0}
                </div>
              </div>
            </div>

            {previewModal.errors?.length > 0 && (
              <div>
                <div className="eyebrow mb-2">{t('import.errorsList')}</div>
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 max-h-80 overflow-y-auto">
                  <ul className="space-y-2 text-sm">
                    {previewModal.errors.map((err, i) => (
                      <li key={i} className="flex items-start gap-2">
                        <span className="text-red-600 shrink-0">•</span>
                        <span className="text-text-muted">{err}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>
        )}
        <ModalFooter>
          <button onClick={() => setPreviewModal(null)} className="btn-secondary">
            {t('common.close')}
          </button>
        </ModalFooter>
      </Modal>

      {importing && <Loader text={t('import.importing')} />}
    </Layout>
  );
}

function formatDateTime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' });
}
