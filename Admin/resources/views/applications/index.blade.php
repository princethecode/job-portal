@extends('layouts.app')

@section('title', 'Applications Management')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Applications Management</h2>
    </div>
    
    <!-- Filters -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Filter Applications</h6>
        </div>
        <div class="card-body">
            <form action="{{ route('admin.applications.index') }}" method="GET">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="job_id" class="form-label">Job</label>
                        <select class="form-select" id="job_id" name="job_id">
                            <option value="">All Jobs</option>
                            @foreach($jobs as $job)
                                <option value="{{ $job->id }}" {{ isset($filters['job_id']) && $filters['job_id'] == $job->id ? 'selected' : '' }}>
                                    {{ $job->title }}
                                </option>
                            @endforeach
                        </select>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="status" class="form-label">Status</label>
                        <select class="form-select" id="status" name="status">
                            <option value="">All Statuses</option>
                            <option value="Applied" {{ isset($filters['status']) && $filters['status'] == 'Applied' ? 'selected' : '' }}>Applied</option>
                            <option value="Under Review" {{ isset($filters['status']) && $filters['status'] == 'Under Review' ? 'selected' : '' }}>Under Review</option>
                            <option value="Shortlisted" {{ isset($filters['status']) && $filters['status'] == 'Shortlisted' ? 'selected' : '' }}>Shortlisted</option>
                            <option value="Rejected" {{ isset($filters['status']) && $filters['status'] == 'Rejected' ? 'selected' : '' }}>Rejected</option>
                        </select>
                    </div>
                </div>
                <div class="d-flex justify-content-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-filter me-1"></i> Apply Filters
                    </button>
                    <a href="{{ route('admin.applications.index') }}" class="btn btn-secondary ms-2">
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
        <!-- Applications List -->
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">All Applications</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Applicant</th>
                                <th>Job</th>
                                <th>Applied Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @forelse($applications as $application)
                                <tr>
                                    <td>{{ $application->id }}</td>
                                    <td>{{ $application->user->name }}</td>
                                    <td>{{ $application->job->title }}</td>
                                    <td>{{ $application->created_at->format('M d, Y') }}</td>
                                    <td>
                                        @switch($application->status)
                                            @case('Applied')
                                                <span class="badge bg-primary">Applied</span>
                                                @break
                                            @case('Under Review')
                                                <span class="badge bg-info">Under Review</span>
                                                @break
                                            @case('Shortlisted')
                                                <span class="badge bg-success">Shortlisted</span>
                                                @break
                                            @case('Rejected')
                                                <span class="badge bg-danger">Rejected</span>
                                                @break
                                            @default
                                                <span class="badge bg-secondary">{{ $application->status }}</span>
                                        @endswitch
                                    </td>
                                    <td>
                                        <div class="btn-group">
                                            <a href="{{ route('admin.applications.show', $application->id) }}" 
                                               class="btn btn-sm btn-info" 
                                               title="View Details">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                            <button type="button" 
                                                    class="btn btn-sm btn-primary" 
                                                    data-bs-toggle="modal" 
                                                    data-bs-target="#statusModal{{ $application->id }}"
                                                    title="Update Status">
                                                <i class="fas fa-edit"></i>
                                            </button>
                                        </div>

                                        <!-- Status Update Modal -->
                                        <div class="modal fade" id="statusModal{{ $application->id }}" tabindex="-1">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <form action="{{ route('admin.applications.update-status', $application->id) }}" method="POST">
                                                        @csrf
                                                        @method('PUT')
                                                        <div class="modal-header">
                                                            <h5 class="modal-title">Update Application Status</h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="mb-3">
                                                                <label for="status{{ $application->id }}" class="form-label">Status</label>
                                                                <select class="form-select" id="status{{ $application->id }}" name="status">
                                                                    <option value="Applied" {{ $application->status == 'Applied' ? 'selected' : '' }}>Applied</option>
                                                                    <option value="Under Review" {{ $application->status == 'Under Review' ? 'selected' : '' }}>Under Review</option>
                                                                    <option value="Shortlisted" {{ $application->status == 'Shortlisted' ? 'selected' : '' }}>Shortlisted</option>
                                                                    <option value="Rejected" {{ $application->status == 'Rejected' ? 'selected' : '' }}>Rejected</option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer">
                                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                            <button type="submit" class="btn btn-primary">Update Status</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            @empty
                                <tr>
                                    <td colspan="6" class="text-center">No applications found</td>
                                </tr>
                            @endforelse
                        </tbody>
                    </table>
                </div>
                
                <!-- Pagination -->
                <div class="d-flex justify-content-center mt-4">
                    {{ $applications->links() }}
                </div>
            </div>
        </div>
    @endif
</div>
@endsection
