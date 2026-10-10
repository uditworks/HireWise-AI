import axiosClient from './axiosClient';

export const applicationsApi = {
  /**
   * Submit a new job application for the authenticated Job Seeker
   * @param {number|string} jobId - Job ID
   * @param {Object} applicationData - { coverLetter?: string, resumeId?: number }
   * @returns {Promise<Object>} JobApplicationResponse
   */
  applyToJob: async (jobId, applicationData = {}) => {
    const payload = {};
    if (applicationData.coverLetter && applicationData.coverLetter.trim()) {
      payload.coverLetter = applicationData.coverLetter.trim();
    }
    if (applicationData.resumeId) {
      payload.resumeId = Number(applicationData.resumeId);
    }

    const response = await axiosClient.post(`/jobs/${jobId}/applications`, payload);
    return response.data;
  },

  /**
   * Fetch paginated list of applications submitted by the authenticated Job Seeker
   * @param {Object} params - { page?: number, size?: number, sort?: string }
   * @returns {Promise<Object>} Page<JobApplicationResponse>
   */
  getMyApplications: async (params = {}) => {
    const queryParams = {
      page: typeof params.page === 'number' ? params.page : 0,
      size: typeof params.size === 'number' ? params.size : 10,
      sort: params.sort || 'createdAt,desc'
    };

    const response = await axiosClient.get('/applications/my-applications', { params: queryParams });
    return response.data;
  },

  /**
   * Withdraw an active application owned by the authenticated Job Seeker
   * @param {number|string} applicationId - Application ID
   * @returns {Promise<Object>} JobApplicationResponse with WITHDRAWN status
   */
  withdrawApplication: async (applicationId) => {
    const response = await axiosClient.patch(`/applications/${applicationId}/withdraw`);
    return response.data;
  },

  /**
   * Fetch single application details by ID
   * @param {number|string} applicationId - Application ID
   * @returns {Promise<Object>} JobApplicationResponse
   */
  getApplicationById: async (applicationId) => {
    const response = await axiosClient.get(`/applications/${applicationId}`);
    return response.data;
  },

  /**
   * Fetch paginated list of applications for a specific job (Recruiter owner only)
   * @param {number|string} jobId - Job ID
   * @param {Object} params - { status?: string, page?: number, size?: number, sort?: string }
   * @returns {Promise<Object>} Page<JobApplicationResponse>
   */
  getApplicationsForJob: async (jobId, params = {}) => {
    const queryParams = {
      page: typeof params.page === 'number' ? params.page : 0,
      size: typeof params.size === 'number' ? params.size : 10,
      sort: params.sort || 'createdAt,desc'
    };

    if (params.status && params.status !== 'ALL') {
      queryParams.status = params.status;
    }

    const response = await axiosClient.get(`/jobs/${jobId}/applications`, { params: queryParams });
    return response.data;
  },

  /**
   * Update applicant review status (Recruiter owner only)
   * @param {number|string} applicationId - Application ID
   * @param {string} status - ApplicationStatus ('SUBMITTED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED')
   * @returns {Promise<Object>} JobApplicationResponse
   */
  updateApplicationStatus: async (applicationId, status) => {
    const response = await axiosClient.patch(`/applications/${applicationId}/status`, { status });
    return response.data;
  }
};
