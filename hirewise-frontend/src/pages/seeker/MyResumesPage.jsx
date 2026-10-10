import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { resumesApi } from '../../api/resumesApi';
import { useToast } from '../../context/ToastContext';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  FileText,
  Upload,
  Sparkles,
  Download,
  Trash2,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Calendar,
  HardDrive,
  Target,
  FileCheck2,
  X
} from 'lucide-react';

export default function MyResumesPage() {
  const { showSuccess, showError } = useToast();
  const fileInputRef = useRef(null);
  const replaceInputRef = useRef(null);

  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Upload states
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [dragActive, setDragActive] = useState(false);

  // Replace / Delete states
  const [replacingResume, setReplacingResume] = useState(null);
  const [replacementFile, setReplacementFile] = useState(null);
  const [replacing, setReplacing] = useState(false);
  const [replaceError, setReplaceError] = useState('');

  const [deletingResume, setDeletingResume] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [downloadingId, setDownloadingId] = useState(null);

  const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

  const fetchResumes = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await resumesApi.getMyResumes();
      setResumes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Could not load your uploaded resumes.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResumes();
  }, []);

  const validateFile = (file) => {
    if (!file) return 'No file selected';
    if (!file.name.toLowerCase().endsWith('.pdf') && file.type !== 'application/pdf') {
      return 'Only PDF files (.pdf) are supported. Please select a valid PDF document.';
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      return `File size (${(file.size / (1024 * 1024)).toFixed(2)} MB) exceeds the maximum allowed limit of 10 MB.`;
    }
    if (file.size === 0) {
      return 'The selected file is empty (0 bytes). Please select a valid resume.';
    }
    return null;
  };

  const handleFileSelect = (file) => {
    setUploadError('');
    const errorMsg = validateFile(file);
    if (errorMsg) {
      setUploadError(errorMsg);
      setSelectedFile(null);
      return;
    }
    setSelectedFile(file);
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      setUploadError('Please choose a PDF resume to upload.');
      return;
    }

    setUploading(true);
    setUploadError('');
    try {
      const uploaded = await resumesApi.uploadResume(selectedFile);
      showSuccess(`Resume "${uploaded.originalFilename}" uploaded and parsed successfully!`);
      setSelectedFile(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
      fetchResumes();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to upload resume.';
      setUploadError(msg);
      showError(msg);
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (resume) => {
    setDownloadingId(resume.id);
    try {
      await resumesApi.downloadResume(resume.id, resume.originalFilename);
      showSuccess(`Downloaded ${resume.originalFilename}`);
    } catch (_err) {
      showError('Failed to download resume file.');
    } finally {
      setDownloadingId(null);
    }
  };

  const handleConfirmReplace = async (e) => {
    e.preventDefault();
    if (!replacementFile || !replacingResume) return;

    setReplacing(true);
    setReplaceError('');
    try {
      await resumesApi.replaceResume(replacingResume.id, replacementFile);
      showSuccess(`Resume replaced successfully with "${replacementFile.name}"!`);
      setReplacingResume(null);
      setReplacementFile(null);
      fetchResumes();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to replace resume.';
      setReplaceError(msg);
      showError(msg);
    } finally {
      setReplacing(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deletingResume) return;

    setDeleting(true);
    try {
      await resumesApi.deleteResume(deletingResume.id);
      showSuccess(`Resume "${deletingResume.originalFilename}" permanently deleted.`);
      setDeletingResume(null);
      fetchResumes();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to delete resume.';
      showError(msg);
    } finally {
      setDeleting(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes && bytes !== 0) return '';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return '';
    }
  };

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 border border-blue-200/80 text-blue-700 text-xs font-semibold uppercase tracking-wider mb-1">
              <Sparkles className="w-3.5 h-3.5 text-blue-600" />
              <span>AI Resume Engine</span>
            </div>
            <h1 className="text-3xl font-extrabold text-navy-950 tracking-tight">
              Resume Management & AI Analysis
            </h1>
            <p className="text-sm text-slate-500">
              Upload PDF resumes for automatic skill extraction, deterministic scoring, and job matching.
            </p>
          </div>
        </div>

        {/* Upload Card / Dropzone */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-2.5 pb-3 border-b border-slate-100">
            <div className="w-8 h-8 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center font-bold">
              <Upload className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-base font-bold text-navy-950">Upload New Resume</h3>
              <p className="text-xs text-slate-500">PDF documents only (maximum size 10 MB)</p>
            </div>
          </div>

          {/* Upload Error Banner */}
          {uploadError && (
            <div className="p-3.5 bg-red-50 border border-red-200 rounded-xl text-xs text-red-800 flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0 mt-0.5" />
              <p>{uploadError}</p>
            </div>
          )}

          {/* Dropzone Container */}
          <div
            onDragOver={(e) => {
              e.preventDefault();
              setDragActive(true);
            }}
            onDragLeave={() => setDragActive(false)}
            onDrop={(e) => {
              e.preventDefault();
              setDragActive(false);
              if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                handleFileSelect(e.dataTransfer.files[0]);
              }
            }}
            className={`border-2 border-dashed rounded-2xl p-6 sm:p-8 text-center transition-all cursor-pointer ${
              dragActive
                ? 'border-blue-500 bg-blue-50/50'
                : selectedFile
                ? 'border-emerald-300 bg-emerald-50/30'
                : 'border-slate-300 hover:border-blue-400 bg-slate-50/50'
            }`}
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,application/pdf"
              className="hidden"
              onChange={(e) => {
                if (e.target.files && e.target.files[0]) {
                  handleFileSelect(e.target.files[0]);
                }
              }}
            />

            {selectedFile ? (
              <div className="flex flex-col items-center space-y-2">
                <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                  <FileCheck2 className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-bold text-navy-950">{selectedFile.name}</p>
                  <p className="text-xs text-slate-500">{formatFileSize(selectedFile.size)} • Ready to upload</p>
                </div>
                <p className="text-[11px] text-blue-600 font-semibold hover:underline pt-1">
                  Click to choose a different file
                </p>
              </div>
            ) : (
              <div className="flex flex-col items-center space-y-2">
                <div className="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center">
                  <Upload className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-navy-950">
                    Drag and drop your PDF resume here, or <span className="text-blue-600 font-bold">browse</span>
                  </p>
                  <p className="text-xs text-slate-400 mt-0.5">Supports PDF documents up to 10 MB</p>
                </div>
              </div>
            )}
          </div>

          {/* Upload Button */}
          {selectedFile && (
            <div className="flex items-center justify-end gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedFile(null);
                  if (fileInputRef.current) fileInputRef.current.value = '';
                }}
                disabled={uploading}
              >
                Clear Selection
              </Button>
              <Button
                type="button"
                variant="navy"
                size="sm"
                onClick={handleUploadSubmit}
                loading={uploading}
              >
                <Sparkles className="w-4 h-4" />
                <span>Upload & Parse Resume</span>
              </Button>
            </div>
          )}
        </div>

        {/* Resumes Catalog */}
        <div className="space-y-4">
          <div className="flex items-center justify-between text-xs text-slate-500 px-1">
            <h3 className="text-base font-bold text-navy-950">Uploaded Resumes ({resumes.length})</h3>
          </div>

          {loading ? (
            <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
              <LoadingSpinner size="lg" />
              <p className="text-sm font-medium text-slate-600">Retrieving your resumes...</p>
            </div>
          ) : error ? (
            <div className="bg-white rounded-2xl border border-red-200 p-10 flex flex-col items-center justify-center text-center space-y-4 shadow-sm">
              <AlertCircle className="w-8 h-8 text-red-600 mx-auto" />
              <p className="text-xs text-slate-600">{error}</p>
              <Button variant="primary" size="sm" onClick={fetchResumes}>
                <span>Try Again</span>
              </Button>
            </div>
          ) : resumes.length === 0 ? (
            /* Empty State */
            <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
              <div className="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center mx-auto">
                <FileText className="w-7 h-7" />
              </div>
              <h4 className="text-lg font-bold text-navy-950">No Resumes Uploaded Yet</h4>
              <p className="text-xs text-slate-500 max-w-sm">
                Upload your PDF resume above to unlock automated skill extraction, strengths discovery, and match compatibility against published jobs.
              </p>
            </div>
          ) : (
            /* Resumes Cards */
            <div className="space-y-4">
              {resumes.map((resume) => (
                <div
                  key={resume.id}
                  className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm hover:border-slate-300 transition-all space-y-4"
                >
                  {/* Card Top */}
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
                    <div className="flex items-center gap-3">
                      <div className="w-11 h-11 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center flex-shrink-0">
                        <FileText className="w-6 h-6" />
                      </div>
                      <div>
                        <h4 className="font-bold text-navy-950 text-base">{resume.originalFilename}</h4>
                        <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 mt-0.5">
                          <span className="flex items-center gap-1 font-mono">
                            <HardDrive className="w-3.5 h-3.5 text-slate-400" />
                            <span>{formatFileSize(resume.fileSize)}</span>
                          </span>
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3.5 h-3.5 text-slate-400" />
                            <span>Uploaded {formatDate(resume.createdAt)}</span>
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <Badge status={resume.processingStatus} size="sm" />
                    </div>
                  </div>

                  {/* Card Actions */}
                  <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
                    <div className="flex flex-wrap items-center gap-2">
                      {/* Analyze Button */}
                      <Link to={`/seeker/resumes/${resume.id}/analyze`}>
                        <Button variant="navy" size="sm">
                          <Sparkles className="w-3.5 h-3.5" />
                          <span>AI Skills Analysis</span>
                        </Button>
                      </Link>

                      {/* Job Match Button */}
                      <Link to={`/seeker/resumes/${resume.id}/match`}>
                        <Button variant="secondary" size="sm">
                          <Target className="w-3.5 h-3.5" />
                          <span>Match against Jobs</span>
                        </Button>
                      </Link>

                      {/* Download Button */}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleDownload(resume)}
                        loading={downloadingId === resume.id}
                      >
                        <Download className="w-3.5 h-3.5" />
                        <span>Download PDF</span>
                      </Button>

                      {/* Replace Button */}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setReplacingResume(resume);
                          setReplacementFile(null);
                          setReplaceError('');
                        }}
                      >
                        <RefreshCw className="w-3.5 h-3.5" />
                        <span>Replace</span>
                      </Button>
                    </div>

                    {/* Delete Trigger */}
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => setDeletingResume(resume)}
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>Delete</span>
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Replace Resume Modal */}
      {replacingResume && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="relative w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-2xl p-6 space-y-4 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-2 border-b border-slate-100">
              <h3 className="text-lg font-bold text-navy-950">Replace Resume</h3>
              <button
                onClick={() => setReplacingResume(null)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-600">
              Select a new PDF file to replace{' '}
              <strong className="text-slate-900">{replacingResume.originalFilename}</strong>.
            </p>

            {replaceError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-800 flex items-start gap-2">
                <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0 mt-0.5" />
                <p>{replaceError}</p>
              </div>
            )}

            <div
              className="border-2 border-dashed border-slate-300 rounded-xl p-4 text-center cursor-pointer hover:border-blue-400"
              onClick={() => replaceInputRef.current?.click()}
            >
              <input
                ref={replaceInputRef}
                type="file"
                accept=".pdf,application/pdf"
                className="hidden"
                onChange={(e) => {
                  if (e.target.files && e.target.files[0]) {
                    const err = validateFile(e.target.files[0]);
                    if (err) setReplaceError(err);
                    else {
                      setReplaceError('');
                      setReplacementFile(e.target.files[0]);
                    }
                  }
                }}
              />
              {replacementFile ? (
                <div className="text-xs text-emerald-700 font-semibold flex items-center justify-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  <span>{replacementFile.name} ({formatFileSize(replacementFile.size)})</span>
                </div>
              ) : (
                <p className="text-xs text-slate-500 font-medium">Click to choose replacement PDF</p>
              )}
            </div>

            <div className="flex items-center justify-end gap-3 pt-2 border-t border-slate-100">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setReplacingResume(null)}
                disabled={replacing}
              >
                Cancel
              </Button>
              <Button
                type="button"
                variant="navy"
                size="sm"
                onClick={handleConfirmReplace}
                disabled={!replacementFile}
                loading={replacing}
              >
                <span>Upload Replacement</span>
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deletingResume && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="relative w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-2xl p-6 space-y-4 animate-in fade-in zoom-in-95">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
              <AlertCircle className="w-6 h-6" />
            </div>

            <div className="space-y-1">
              <h3 className="text-lg font-bold text-navy-950">Delete Resume Document?</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Are you sure you want to permanently delete{' '}
                <strong className="text-slate-900 font-semibold">"{deletingResume.originalFilename}"</strong>?
                This will remove the file and all associated AI skill extractions.
              </p>
            </div>

            <div className="pt-2 flex items-center justify-end gap-3 border-t border-slate-100">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setDeletingResume(null)}
                disabled={deleting}
              >
                Cancel
              </Button>
              <Button
                type="button"
                variant="danger"
                size="sm"
                onClick={handleConfirmDelete}
                loading={deleting}
              >
                <span>Delete Resume</span>
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
