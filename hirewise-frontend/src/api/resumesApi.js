import axiosClient from './axiosClient';

export const resumesApi = {
  /**
   * Upload a PDF resume (Job Seeker only)
   * @param {File} file - PDF resume file (max 10 MB)
   * @returns {Promise<Object>} ResumeResponse
   */
  uploadResume: async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axiosClient.post('/resumes/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  /**
   * Fetch all resumes uploaded by the authenticated Job Seeker
   * @returns {Promise<Array>} List of ResumeSummaryResponse
   */
  getMyResumes: async () => {
    const response = await axiosClient.get('/resumes/my');
    return response.data;
  },

  /**
   * Fetch single resume details by ID
   * @param {number|string} id - Resume ID
   * @returns {Promise<Object>} ResumeResponse
   */
  getResumeById: async (id) => {
    const response = await axiosClient.get(`/resumes/${id}`);
    return response.data;
  },

  /**
   * Securely download a resume PDF via authenticated Blob response
   * @param {number|string} id - Resume ID
   * @param {string} fallbackFilename - Optional filename to save as
   */
  downloadResume: async (id, fallbackFilename = 'resume.pdf') => {
    const response = await axiosClient.get(`/resumes/${id}/download`, {
      responseType: 'blob'
    });

    const blobUrl = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', fallbackFilename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(blobUrl);
  },

  /**
   * Replace an existing resume with a new PDF file (Job Seeker only)
   * @param {number|string} id - Resume ID
   * @param {File} file - New PDF resume file
   * @returns {Promise<Object>} ResumeResponse
   */
  replaceResume: async (id, file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axiosClient.put(`/resumes/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  /**
   * Delete a resume (Job Seeker only)
   * @param {number|string} id - Resume ID
   * @returns {Promise<void>}
   */
  deleteResume: async (id) => {
    await axiosClient.delete(`/resumes/${id}`);
  },

  /**
   * Retrieve AI deterministic analysis for a resume
   * @param {number|string} id - Resume ID
   * @returns {Promise<Object>} ResumeAnalysisResponse
   */
  analyzeResume: async (id) => {
    const response = await axiosClient.get(`/resumes/${id}/analyze`);
    return response.data;
  },

  /**
   * Match a resume against a specific published job
   * @param {number|string} id - Resume ID
   * @param {number|string} jobId - Job ID
   * @returns {Promise<Object>} JobMatchResponse
   */
  matchResumeToJob: async (id, jobId) => {
    const response = await axiosClient.get(`/resumes/${id}/match/${jobId}`);
    return response.data;
  }
};
