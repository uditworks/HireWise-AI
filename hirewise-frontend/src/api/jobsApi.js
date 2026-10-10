import axiosClient from './axiosClient';

export const jobsApi = {
  /**
   * Fetch paginated list of published jobs with optional search filters
   * @param {Object} filters - { keyword, location, employmentType, page, size, sort }
   * @returns {Promise<Object>} Spring Page object: { content, totalPages, totalElements, number, size, ... }
   */
  getPublishedJobs: async (filters = {}) => {
    const params = {};

    if (filters.keyword && filters.keyword.trim()) {
      params.keyword = filters.keyword.trim();
    }
    if (filters.location && filters.location.trim()) {
      params.location = filters.location.trim();
    }
    if (filters.employmentType && filters.employmentType !== 'ALL') {
      params.employmentType = filters.employmentType;
    }
    if (typeof filters.page === 'number') {
      params.page = filters.page;
    }
    if (typeof filters.size === 'number') {
      params.size = filters.size;
    }
    if (filters.sort) {
      params.sort = filters.sort;
    }

    const response = await axiosClient.get('/jobs', { params });
    return response.data;
  },

  /**
   * Fetch published job details by ID (or recruiter-owned job)
   * @param {number|string} id - Job ID
   * @returns {Promise<Object>} JobResponse object
   */
  getJobById: async (id) => {
    const response = await axiosClient.get(`/jobs/${id}`);
    return response.data;
  },

  /**
   * Fetch paginated list of jobs owned by the authenticated recruiter
   * @param {Object} params - { page?: number, size?: number, sort?: string }
   * @returns {Promise<Object>} Page<JobResponse>
   */
  getMyJobs: async (params = {}) => {
    const queryParams = {
      page: typeof params.page === 'number' ? params.page : 0,
      size: typeof params.size === 'number' ? params.size : 10,
      sort: params.sort || 'createdAt,desc'
    };

    const response = await axiosClient.get('/jobs/my-jobs', { params: queryParams });
    return response.data;
  },

  /**
   * Create a new job posting (Recruiter only)
   * @param {Object} jobData - { title, description, location, employmentType, salaryRange, requiredSkills, publishImmediately }
   * @returns {Promise<Object>} JobResponse
   */
  createJob: async (jobData) => {
    const response = await axiosClient.post('/jobs', jobData);
    return response.data;
  },

  /**
   * Update an existing job posting (Recruiter owner only)
   * @param {number|string} id - Job ID
   * @param {Object} jobData - { title, description, location, employmentType, salaryRange, requiredSkills }
   * @returns {Promise<Object>} JobResponse
   */
  updateJob: async (id, jobData) => {
    const response = await axiosClient.put(`/jobs/${id}`, jobData);
    return response.data;
  },

  /**
   * Update status of a job posting (DRAFT, PUBLISHED, CLOSED)
   * @param {number|string} id - Job ID
   * @param {string} status - JobStatus ('DRAFT' | 'PUBLISHED' | 'CLOSED')
   * @returns {Promise<Object>} JobResponse
   */
  updateJobStatus: async (id, status) => {
    const response = await axiosClient.patch(`/jobs/${id}/status`, { status });
    return response.data;
  },

  /**
   * Delete a job posting (Recruiter owner only)
   * @param {number|string} id - Job ID
   * @returns {Promise<void>}
   */
  deleteJob: async (id) => {
    await axiosClient.delete(`/jobs/${id}`);
  }
};
