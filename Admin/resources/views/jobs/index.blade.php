@extends('layouts.app')

@section('title', 'Jobs Management')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Jobs Management</h2>
        <a href="{{ route('admin.jobs.create') }}" class="btn btn-primary">
            <i class="fas fa-plus me-1"></i> Add New Job
        </a>
    </div>
    
    <!-- Filters -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Filter Jobs</h6>
        </div>
        <div class="card-body">
            <form action="{{ route('admin.jobs.index') }}" method="GET">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <label for="search" class="form-label">Search</label>
                        <input type="text" class="form-control" id="search" name="search" value="{{ $filters['search'] ?? '' }}" placeholder="Job title, company...">
                    </div>
                    <div class="col-md-3 mb-3">
                        <label for="location" class="form-label">Location</label>
                        <input type="text" class="form-control" id="location" name="location" value="{{ $filters['location'] ?? '' }}" placeholder="City, Country...">
                    </div>
                    <div class="col-md-3 mb-3">
                        <label for="job_type" class="form-label">Job Type</label>
                        <select class="form-select" id="job_type" name="job_type">
                            <option value="">All Types</option>
                            <option value="Full-time" {{ isset($filters['job_type']) && $filters['job_type'] == 'Full-time' ? 'selected' : '' }}>Full-time</option>
                            <option value="Part-time" {{ isset($filters['job_type']) && $filters['job_type'] == 'Part-time' ? 'selected' : '' }}>Part-time</option>
                            <option value="Contract" {{ isset($filters['job_type']) && $filters['job_type'] == 'Contract' ? 'selected' : '' }}>Contract</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label for="category" class="form-label">Category</label>
                        <input type="text" class="form-control" id="category" name="category" value="{{ $filters['category'] ?? '' }}" placeholder="IT, Finance...">
                    </div>
                </div>
                <div class="d-flex justify-content-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-filter me-1"></i> Apply Filters
                    </button>
                    <a href="{{ route('admin.jobs.index') }}" class="btn btn-secondary ms-2">
                        <i class="fas fa-redo me-1"></i> Reset
                    </a>
                </div>
            </form>
        </div>
    </div>
    
    @if(isset($error))
        <div class="alert alert-danger">
            {{ $error }}
        </div>
    @else
        <!-- Jobs List -->
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">All Jobs</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Company</th>
                                <th>Location</th>
                                <th>Job Type</th>
                                <th>Category</th>
                                <th>Posting Date</th>
                                <th>Expiry Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @forelse($jobs as $job)
                                <tr>
                                    <td>{{ $job->id }}</td>
                                    <td>{{ $job->title }}</td>
                                    <td>{{ $job->company }}</td>
                                    <td>{{ $job->location }}</td>
                                    <td>{{ $job->job_type }}</td>
                                    <td>{{ $job->category }}</td>
                                    <td>{{ $job->posting_date->format('M d, Y') }}</td>
                                    <td>{{ $job->expiry_date->format('M d, Y') }}</td>
                                    <td>
                                        @if($job->is_active)
                                            <span class="badge bg-success">Active</span>
                                        @else
                                            <span class="badge bg-danger">Inactive</span>
                                        @endif
                                    </td>
                                    <td>
                                        <div class="btn-group" role="group">
                                            <a href="{{ route('admin.jobs.show', $job->id) }}" 
                                               class="btn btn-sm btn-info" 
                                               title="View Details">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                            <a href="{{ route('admin.jobs.edit', $job->id) }}" 
                                               class="btn btn-sm btn-warning"
                                               title="Edit Job">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <button type="button" 
                                                    class="btn btn-sm btn-danger" 
                                                    data-bs-toggle="modal" 
                                                    data-bs-target="#deleteModal{{ $job->id }}"
                                                    title="Delete Job">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </div>
                                        
                                        <!-- Delete Modal -->
                                        <div class="modal fade" id="deleteModal{{ $job->id }}" tabindex="-1">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">Confirm Delete</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        Are you sure you want to delete the job "{{ $job->title }}"?
                                                        <p class="text-danger mt-2 mb-0">
                                                            <i class="fas fa-exclamation-triangle"></i>
                                                            This will also delete all associated applications.
                                                        </p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                        <form action="{{ route('admin.jobs.destroy', $job->id) }}" method="POST">
                                                            @csrf
                                                            @method('DELETE')
                                                            <button type="submit" class="btn btn-danger">Delete</button>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            @empty
                                <tr>
                                    <td colspan="10" class="text-center">No jobs found</td>
                                </tr>
                            @endforelse
                        </tbody>
                    </table>
                </div>
                
                <!-- Pagination -->
                <div class="d-flex justify-content-center mt-4">
                    {{ $jobs->links() }}
                </div>
            </div>
        </div>
    @endif
</div>
@endsection
