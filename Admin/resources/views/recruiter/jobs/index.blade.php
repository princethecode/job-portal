@extends('recruiter.layouts.app')

@section('title', 'My Jobs')
@section('page-title', 'My Jobs')

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h4>Manage Your Job Postings</h4>
        <p class="text-muted">Create, edit, and manage your job listings</p>
    </div>
    <a href="{{ route('recruiter.jobs.create') }}" class="btn btn-primary">
        <i class="fas fa-plus me-2"></i>Post New Job
    </a>
</div>

<!-- Filters -->
<div class="card mb-4">
    <div class="card-body">
        <form method="GET" action="{{ route('recruiter.jobs.index') }}">
            <div class="row">
                <div class="col-md-4">
                    <input type="text" class="form-control" name="search" 
                           placeholder="Search jobs..." value="{{ request('search') }}">
                </div>
                <div class="col-md-3">
                    <select class="form-select" name="status">
                        <option value="">All Status</option>
                        <option value="active" {{ request('status') === 'active' ? 'selected' : '' }}>Active</option>
                        <option value="inactive" {{ request('status') === 'inactive' ? 'selected' : '' }}>Inactive</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <select class="form-select" name="job_type">
                        <option value="">All Types</option>
                        <option value="Full-time" {{ request('job_type') === 'Full-time' ? 'selected' : '' }}>Full-time</option>
                        <option value="Part-time" {{ request('job_type') === 'Part-time' ? 'selected' : '' }}>Part-time</option>
                        <option value="Contract" {{ request('job_type') === 'Contract' ? 'selected' : '' }}>Contract</option>
                        <option value="Internship" {{ request('job_type') === 'Internship' ? 'selected' : '' }}>Internship</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-outline-primary w-100">
                        <i class="fas fa-search me-1"></i>Filter
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- Jobs List -->
<div class="row">
    @forelse($jobs as $job)
    <div class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h5 class="card-title">{{ $job->title }}</h5>
                    <span class="badge bg-{{ $job->is_active ? 'success' : 'secondary' }}">
                        {{ $job->is_active ? 'Active' : 'Inactive' }}
                    </span>
                </div>
                
                <p class="card-text text-muted">
                    <i class="fas fa-map-marker-alt me-1"></i>{{ $job->location }}
                </p>
                
                <p class="card-text text-muted">
                    <i class="fas fa-briefcase me-1"></i>{{ $job->job_type }}
                </p>
                
                @if($job->salary)
                <p class="card-text text-muted">
                    <i class="fas fa-dollar-sign me-1"></i>${{ number_format($job->salary) }}
                </p>
                @endif
                
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <small class="text-muted">
                        <i class="fas fa-users me-1"></i>{{ $job->applications_count }} applications
                    </small>
                    <small class="text-muted">
                        Posted {{ $job->created_at->diffForHumans() }}
                    </small>
                </div>
                
                <div class="btn-group w-100" role="group">
                    <a href="{{ route('recruiter.jobs.show', $job) }}" class="btn btn-outline-primary btn-sm">
                        <i class="fas fa-eye me-1"></i>View
                    </a>
                    <a href="{{ route('recruiter.jobs.edit', $job) }}" class="btn btn-outline-secondary btn-sm">
                        <i class="fas fa-edit me-1"></i>Edit
                    </a>
                    <form method="POST" action="{{ route('recruiter.jobs.toggle-status', $job) }}" class="d-inline">
                        @csrf
                        <button type="submit" class="btn btn-outline-{{ $job->is_active ? 'warning' : 'success' }} btn-sm">
                            <i class="fas fa-{{ $job->is_active ? 'pause' : 'play' }} me-1"></i>
                            {{ $job->is_active ? 'Pause' : 'Activate' }}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    @empty
    <div class="col-12">
        <div class="text-center py-5">
            <i class="fas fa-briefcase fa-3x text-muted mb-3"></i>
            <h4>No Jobs Posted Yet</h4>
            <p class="text-muted">Start by posting your first job to attract candidates.</p>
            <a href="{{ route('recruiter.jobs.create') }}" class="btn btn-primary">
                <i class="fas fa-plus me-2"></i>Post Your First Job
            </a>
        </div>
    </div>
    @endforelse
</div>

<!-- Pagination -->
@if($jobs->hasPages())
<div class="d-flex justify-content-center">
    {{ $jobs->links() }}
</div>
@endif
@endsection