import React, { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { jobsApi } from '../../api/jobsApi';
import { EMPLOYMENT_TYPES, EMPLOYMENT_TYPE_LABELS } from '../../utils/constants';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  Search,
  MapPin,
  Briefcase,
  DollarSign,
  Calendar,
  Sparkles,
  RotateCcw,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Building,
  ArrowRight
} from 'lucide-react';

export default function JobsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const urlKeyword = searchParams.get('keyword') || '';
  const urlLocation = searchParams.get('location') || '';
  const urlEmploymentType = searchParams.get('employmentType') || 'ALL';
  const urlPage = Number(searchParams.get('page')) || 0;

  // Search filter form inputs
  const [keyword, setKeyword] = useState(urlKeyword);
  const [location, setLocation] = useState(urlLocation);
  const [employmentType, setEmploymentType] = useState(urlEmploymentType);
  const pageSize = 10;

  // Data states
  const [jobsData, setJobsData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Synchronize form inputs if URL searchParams change (e.g. back/forward, external navigation, reset)
  useEffect(() => {
    setKeyword(urlKeyword);
    setLocation(urlLocation);
    setEmploymentType(urlEmploymentType);
  }, [urlKeyword, urlLocation, urlEmploymentType]);

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await jobsApi.getPublishedJobs({
        keyword: urlKeyword.trim(),
        location: urlLocation.trim(),
        employmentType: urlEmploymentType !== 'ALL' ? urlEmploymentType : undefined,
        page: urlPage,
        size: pageSize,
        sort: 'createdAt,desc'
      });
      setJobsData(data);
    } catch (err) {
      setError(err.message || 'Failed to load job opportunities. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [urlKeyword, urlLocation, urlEmploymentType, urlPage]);

  useEffect(() => {
    fetchJobs();
  }, [fetchJobs]);

  // Handle Search Submission
  const handleSearch = (e) => {
    e.preventDefault();
    const newParams = {};
    if (keyword.trim()) newParams.keyword = keyword.trim();
    if (location.trim()) newParams.location = location.trim();
    if (employmentType !== 'ALL') newParams.employmentType = employmentType;
    // Reset to page 0 on new search
    setSearchParams(newParams);
  };

  // Reset Filters
  const handleResetFilters = () => {
    setKeyword('');
    setLocation('');
    setEmploymentType('ALL');
    setSearchParams({});
  };

  // Pagination Handlers
  const handlePrevPage = () => {
    if (urlPage > 0) {
      const newParams = Object.fromEntries(searchParams.entries());
      const prevPage = urlPage - 1;
      if (prevPage === 0) {
        delete newParams.page;
      } else {
        newParams.page = String(prevPage);
      }
      setSearchParams(newParams);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleNextPage = () => {
    if (jobsData && !jobsData.last) {
      const newParams = Object.fromEntries(searchParams.entries());
      newParams.page = String(urlPage + 1);
      setSearchParams(newParams);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
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

  const jobsList = jobsData?.content || [];
  const totalElements = jobsData?.totalElements || 0;
  const totalPages = jobsData?.totalPages || 0;
  const isFiltered = Boolean(urlKeyword || urlLocation || (urlEmploymentType && urlEmploymentType !== 'ALL'));

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Page Header */}
        <div className="space-y-2 text-center sm:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 border border-blue-200/80 text-blue-700 text-xs font-semibold uppercase tracking-wider mb-1">
            <Sparkles className="w-3.5 h-3.5 text-blue-600" />
            <span>Opportunities Hub</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-navy-950 tracking-tight">
            Explore Open Positions
          </h1>
          <p className="text-sm text-slate-500 max-w-2xl">
            Browse verified job postings matching your technical skillset with transparent salary expectations and clear requirements.
          </p>
        </div>

        {/* Search & Filter Bar */}
        <div className="bg-white p-5 sm:p-6 rounded-2xl border border-slate-200 shadow-sm">
          <form onSubmit={handleSearch} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-3.5">
              {/* Keyword Filter */}
              <div className="md:col-span-4">
                <Input
                  id="job-keyword"
                  name="keyword"
                  placeholder="Job title, skill, or keyword..."
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  icon={<Search className="w-4 h-4 text-slate-400" />}
                />
              </div>

              {/* Location Filter */}
              <div className="md:col-span-3">
                <Input
                  id="job-location"
                  name="location"
                  placeholder="City, State, or Remote..."
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  icon={<MapPin className="w-4 h-4 text-slate-400" />}
                />
              </div>

              {/* Employment Type Dropdown */}
              <div className="md:col-span-3">
                <div className="relative">
                  <select
                    id="job-employment-type"
                    name="employmentType"
                    value={employmentType}
                    onChange={(e) => setEmploymentType(e.target.value)}
                    className="w-full rounded-xl border border-slate-300 bg-white text-slate-900 text-sm px-3.5 py-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-600"
                  >
                    <option value="ALL">All Employment Types</option>
                    {Object.entries(EMPLOYMENT_TYPES).map(([key, value]) => (
                      <option key={key} value={value}>
                        {EMPLOYMENT_TYPE_LABELS[key] || value}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="md:col-span-2 flex items-center gap-2">
                <Button type="submit" variant="navy" className="flex-1">
                  <Search className="w-4 h-4" />
                  <span>Search</span>
                </Button>
                {isFiltered && (
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleResetFilters}
                    title="Reset Filters"
                    className="px-3"
                  >
                    <RotateCcw className="w-4 h-4 text-slate-500" />
                  </Button>
                )}
              </div>
            </div>
          </form>
        </div>

        {/* Results Metadata Bar */}
        <div className="flex items-center justify-between text-xs text-slate-500 px-1">
          <div>
            {!loading && (
              <span>
                Showing <strong className="font-semibold text-navy-950">{jobsList.length}</strong> of{' '}
                <strong className="font-semibold text-navy-950">{totalElements}</strong> published{' '}
                {totalElements === 1 ? 'position' : 'positions'}
              </span>
            )}
          </div>
          {isFiltered && (
            <button
              onClick={handleResetFilters}
              className="text-blue-600 hover:text-blue-800 font-medium hover:underline flex items-center gap-1"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Clear active filters</span>
            </button>
          )}
        </div>

        {/* Content Viewport */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Retrieving published job postings...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-2xl border border-red-200 p-10 flex flex-col items-center justify-center text-center space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div className="max-w-md">
              <h3 className="text-base font-bold text-slate-900">Failed to Load Positions</h3>
              <p className="text-xs text-slate-500 mt-1">{error}</p>
            </div>
            <Button variant="primary" size="sm" onClick={fetchJobs}>
              <span>Try Again</span>
            </Button>
          </div>
        ) : jobsList.length === 0 ? (
          /* Empty State */
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-4">
            <div className="w-14 h-14 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center">
              <Briefcase className="w-7 h-7" />
            </div>
            <div className="max-w-md space-y-1">
              <h3 className="text-lg font-bold text-navy-950">No Published Jobs Found</h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                {isFiltered
                  ? 'No published positions matched your current search filters. Try broadening your keywords or clearing the location filter.'
                  : 'There are currently no active job postings available in the system. Check back soon for new opportunities.'}
              </p>
            </div>
            {isFiltered && (
              <Button variant="outline" size="sm" onClick={handleResetFilters}>
                <RotateCcw className="w-4 h-4" />
                <span>Reset All Filters</span>
              </Button>
            )}
          </div>
        ) : (
          /* Job Cards Grid */
          <div className="space-y-4">
            {jobsList.map((job) => (
              <div
                key={job.id}
                className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm hover:border-blue-300 hover:shadow-md transition-all group flex flex-col justify-between gap-5"
              >
                <div className="space-y-3">
                  {/* Top row: Title + Employment Type Badge */}
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
                    <div>
                      <Link
                        to={`/jobs/${job.id}`}
                        className="text-lg sm:text-xl font-bold text-navy-950 group-hover:text-blue-600 transition-colors"
                      >
                        {job.title}
                      </Link>
                      <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 mt-1">
                        {job.recruiter?.fullName && (
                          <span className="flex items-center gap-1 font-medium text-slate-700">
                            <Building className="w-3.5 h-3.5 text-slate-400" />
                            <span>Posted by {job.recruiter.fullName}</span>
                          </span>
                        )}
                        {job.location && (
                          <span className="flex items-center gap-1">
                            <MapPin className="w-3.5 h-3.5 text-slate-400" />
                            <span>{job.location}</span>
                          </span>
                        )}
                        {job.salaryRange && (
                          <span className="flex items-center gap-1 text-emerald-700 font-semibold">
                            <DollarSign className="w-3.5 h-3.5 text-emerald-600" />
                            <span>{job.salaryRange}</span>
                          </span>
                        )}
                        {job.createdAt && (
                          <span className="flex items-center gap-1 text-slate-400">
                            <Calendar className="w-3.5 h-3.5" />
                            <span>{formatDate(job.createdAt)}</span>
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-2 self-start sm:self-auto">
                      <Badge
                        status={job.employmentType}
                        label={EMPLOYMENT_TYPE_LABELS[job.employmentType] || job.employmentType}
                        size="sm"
                      />
                    </div>
                  </div>

                  {/* Description preview */}
                  <p className="text-xs sm:text-sm text-slate-600 line-clamp-2 leading-relaxed">
                    {job.description}
                  </p>

                  {/* Skills badges */}
                  {job.requiredSkills && job.requiredSkills.length > 0 && (
                    <div className="flex flex-wrap items-center gap-1.5 pt-1">
                      {Array.from(job.requiredSkills).slice(0, 6).map((skill, idx) => (
                        <span
                          key={idx}
                          className="px-2.5 py-0.5 rounded-lg bg-slate-100 text-slate-700 text-[11px] font-medium border border-slate-200/60"
                        >
                          {skill}
                        </span>
                      ))}
                      {job.requiredSkills.length > 6 && (
                        <span className="text-[11px] text-slate-400 font-medium pl-1">
                          +{job.requiredSkills.length - 6} more
                        </span>
                      )}
                    </div>
                  )}
                </div>

                {/* Card Footer Action */}
                <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-[11px] text-slate-400 font-mono">Job ID: #{job.id}</span>
                  <Link to={`/jobs/${job.id}`}>
                    <Button variant="navy" size="sm" className="group-hover:bg-blue-600 transition-colors">
                      <span>View Full Details</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Button>
                  </Link>
                </div>
              </div>
            ))}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="pt-6 flex items-center justify-between border-t border-slate-200">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handlePrevPage}
                  disabled={page === 0}
                >
                  <ChevronLeft className="w-4 h-4" />
                  <span>Previous</span>
                </Button>

                <div className="text-xs font-semibold text-slate-600">
                  Page <span className="text-navy-950 font-bold">{page + 1}</span> of{' '}
                  <span className="text-navy-950 font-bold">{totalPages}</span>
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleNextPage}
                  disabled={jobsData?.last}
                >
                  <span>Next</span>
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
