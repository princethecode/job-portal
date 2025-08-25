@extends('layouts.app')

@section('title', 'Pending Job Approvals')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Pending Job Approvals</h2>
        <div>
            <a href="{{ route('admin.jobs.index') }}" class="btn btn-secondary">
                <i class="fas fa-list me-1"></i> All Jobs
            </a>
        </div>
    </div>
    
    <!-- Quick Stats -->
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-warning shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                Pending Approval</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $jobs->total() }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-clock fa-2x text-warning"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Search/Filter -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Filter Pending Jobs</h6>
        </div>
        <div class="card-body">
            <form action="{{ route('admin.jobs.pending-approval') }}" method="GET">
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label for="search" class="form-label">Search</label>
                        <input type="text" class="form-control" id="search" name="search" value="{{ $filters['search'] ?? '' }}" placeholder="Job title, company...">
                    </div>
                    <div class="col-md-8 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary me-2">
                            <i class="fas fa-search me-1"></i> Search
                        </button>
                        <a href="{{ route('admin.jobs.pending-approval') }}" class="btn btn-secondary">
                            <i class="fas fa-redo me-1"></i> Reset
                        </a>
                    </div>
                </div>
            </form>
        </div>
    </div>
    
    @if(isset($error))
        <div class="alert alert-danger">
            {{ $error }}
        </div>
    @else
        <!-- Pending Jobs List -->
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Jobs Awaiting Approval</h6>
            </div>
            <div class="card-body">
                @forelse($jobs as $job)
                    <div class="card mb-3">
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-8">
                                    <h5 class="card-title">{{ $job->title }}</h5>
                                    <h6 class="card-subtitle mb-2 text-muted">
                                        {{ $job->company_name ?? $job->company }} • {{ $job->location }}
                                    </h6>
                                    <p class="card-text">
                                        <strong>Job Type:</strong> {{ $job->job_type }} <br>
                                        <strong>Category:</strong> {{ $job->category }} <br>
                                        <strong>Salary:</strong> {{ $job->salary ?? 'Not specified' }} <br>
                                        <strong>Experience Level:</strong> {{ $job->experience_level ?? 'Not specified' }}
                                    </p>
                                    <p class="card-text">
                                        <small class="text-muted">
                                            Posted by: 
                                            @if($job->recruiter)
                                                {{ $job->recruiter->company_name }} ({{ $job->recruiter->email }})
                                            @else
                                                System Admin
                                            @endif
                                            on {{ $job->created_at->format('M d, Y \\a\\t g:i A') }}
                                        </small>
                                    </p>
                                    
                                    <!-- Job Description -->
                                    <div class="mt-3">
                                        <h6>Description:</h6>
                                        <p class="text-muted">{{ Str::limit($job->description, 200) }}</p>
                                    </div>
                                    
                                    @if($job->requirements)
                                        <div class="mt-2">
                                            <h6>Requirements:</h6>
                                            <p class="text-muted">{{ Str::limit($job->requirements, 150) }}</p>
                                        </div>
                                    @endif
                                </div>
                                
                                <div class="col-md-4">
                                    <div class="d-flex flex-column h-100">
                                        <div class="mb-auto">
                                            <span class="badge bg-warning text-dark mb-2">
                                                <i class="fas fa-clock me-1"></i> Pending Approval
                                            </span>
                                            <br>
                                            <small class="text-muted">
                                                Expires: {{ $job->expiry_date->format('M d, Y') }}
                                            </small>
                                        </div>
                                        
                                        <div class="mt-3">
                                            <a href="{{ route('admin.jobs.show', $job->id) }}" 
                                               class="btn btn-info btn-sm mb-2 w-100">
                                                <i class="fas fa-eye me-1"></i> View Details
                                            </a>
                                            
                                            <div class="d-grid gap-2">
                                                <button type="button" 
                                                        class="btn btn-success btn-sm" 
                                                        data-bs-toggle="modal" 
                                                        data-bs-target="#approveModal{{ $job->id }}">
                                                    <i class="fas fa-check me-1"></i> Approve
                                                </button>
                                                
                                                <button type="button" 
                                                        class="btn btn-danger btn-sm" 
                                                        data-bs-toggle="modal" 
                                                        data-bs-target="#declineModal{{ $job->id }}">
                                                    <i class="fas fa-times me-1"></i> Decline
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Approve Modal -->
                    <div class="modal fade" id="approveModal{{ $job->id }}" tabindex="-1">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title">Approve Job Posting</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                </div>
                                <div class="modal-body">
                                    <p>Are you sure you want to approve the job posting:</p>
                                    <h6>"{{ $job->title }}"</h6>
                                    <p class="text-muted">by {{ $job->company_name ?? $job->company }}</p>
                                    <div class="alert alert-info">
                                        <i class="fas fa-info-circle me-1"></i>
                                        This job will become visible to job seekers and they can start applying.
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                    <form action="{{ route('admin.jobs.approve', $job->id) }}" method="POST">
                                        @csrf
                                        <button type="submit" class="btn btn-success">
                                            <i class="fas fa-check me-1"></i> Approve Job
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Decline Modal -->
                    <div class="modal fade" id="declineModal{{ $job->id }}" tabindex="-1">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title">Decline Job Posting</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                </div>
                                <form action="{{ route('admin.jobs.decline', $job->id) }}" method="POST">
                                    @csrf
                                    <div class="modal-body">
                                        <p>Decline the job posting:</p>
                                        <h6>"{{ $job->title }}"</h6>
                                        <p class="text-muted">by {{ $job->company_name ?? $job->company }}</p>
                                        
                                        <div class="mb-3">
                                            <label for="decline_reason{{ $job->id }}" class="form-label">
                                                <strong>Reason for decline <span class="text-danger">*</span></strong>
                                            </label>
                                            <textarea class="form-control" 
                                                      id="decline_reason{{ $job->id }}" 
                                                      name="decline_reason" 
                                                      rows="4" 
                                                      placeholder="Please provide a detailed reason for declining this job posting..."
                                                      required></textarea>
                                            <div class="form-text">
                                                This reason will be sent to the recruiter via email.
                                            </div>
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                        <button type="submit" class="btn btn-danger">
                                            <i class="fas fa-times me-1"></i> Decline Job
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                @empty
                    <div class="text-center py-5">
                        <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
                        <h5>No Pending Approvals</h5>
                        <p class="text-muted">All job postings have been reviewed!</p>
                    </div>
                @endforelse
                
                <!-- Pagination -->
                @if($jobs->hasPages())
                    <div class="d-flex justify-content-center">
                        {{ $jobs->links() }}
                    </div>
                @endif
            </div>
        </div>
    @endif
</div>
@endsection

@push('scripts')
<script>
    // Auto-dismiss alerts after 5 seconds
    setTimeout(function() {
        $('.alert').fadeOut('slow');
    }, 5000);
</script>
@endpush