@extends('recruiter.layouts.app')

@section('title', 'Applications')
@section('page-title', 'Applications Management')

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h4>Manage Applications</h4>
        <p class="text-muted">Review and process job applications</p>
    </div>
</div>

<!-- Filters -->
<div class="card mb-4">
    <div class="card-body">
        <form method="GET" action="{{ route('recruiter.applications.index') }}">
            <div class="row">
                <div class="col-md-3">
                    <input type="text" class="form-control" name="search" 
                           placeholder="Search candidates..." value="{{ request('search') }}">
                </div>
                <div class="col-md-3">
                    <select class="form-select" name="status">
                        <option value="">All Status</option>
                        <option value="applied" {{ request('status') === 'applied' ? 'selected' : '' }}>Applied</option>
                        <option value="under_review" {{ request('status') === 'under_review' ? 'selected' : '' }}>Under Review</option>
                        <option value="shortlisted" {{ request('status') === 'shortlisted' ? 'selected' : '' }}>Shortlisted</option>
                        <option value="rejected" {{ request('status') === 'rejected' ? 'selected' : '' }}>Rejected</option>
                        <option value="hired" {{ request('status') === 'hired' ? 'selected' : '' }}>Hired</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <select class="form-select" name="job_id">
                        <option value="">All Jobs</option>
                        @foreach($jobs as $job)
                            <option value="{{ $job->id }}" {{ request('job_id') == $job->id ? 'selected' : '' }}>
                                {{ $job->title }}
                            </option>
                        @endforeach
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

<!-- Bulk Actions -->
@if($applications->count() > 0)
<div class="card mb-4">
    <div class="card-body">
        <form method="POST" action="{{ route('recruiter.applications.bulk-update-status') }}" id="bulk-form">
            @csrf
            <div class="row align-items-center">
                <div class="col-md-3">
                    <label class="form-label">Bulk Actions:</label>
                </div>
                <div class="col-md-3">
                    <select class="form-select" name="status" required>
                        <option value="">Select Status</option>
                        <option value="under_review">Under Review</option>
                        <option value="shortlisted">Shortlisted</option>
                        <option value="rejected">Rejected</option>
                        <option value="hired">Hired</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <button type="submit" class="btn btn-warning" onclick="return confirmBulkUpdate()">
                        <i class="fas fa-edit me-1"></i>Update Selected
                    </button>
                </div>
                <div class="col-md-3">
                    <small class="text-muted">
                        <span id="selected-count">0</span> applications selected
                    </small>
                </div>
            </div>
        </form>
    </div>
</div>
@endif

<!-- Applications List -->
<div class="card shadow">
    <div class="card-body">
        @if($applications->count() > 0)
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>
                                <input type="checkbox" id="select-all" onchange="toggleAll()">
                            </th>
                            <th>Candidate</th>
                            <th>Job</th>
                            <th>Status</th>
                            <th>Applied Date</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        @foreach($applications as $application)
                        <tr>
                            <td>
                                <input type="checkbox" name="application_ids[]" value="{{ $application->id }}" 
                                       form="bulk-form" class="application-checkbox" onchange="updateSelectedCount()">
                            </td>
                            <td>
                                <div class="d-flex align-items-center">
                                    @if($application->user->profile_photo)
                                        <img src="{{ asset('storage/' . $application->user->profile_photo) }}" 
                                             alt="Profile" class="rounded-circle me-2" style="width: 40px; height: 40px;">
                                    @else
                                        <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center me-2" 
                                             style="width: 40px; height: 40px;">
                                            <span class="text-white">{{ substr($application->user->name, 0, 1) }}</span>
                                        </div>
                                    @endif
                                    <div>
                                        <strong>{{ $application->user->name }}</strong>
                                        <br>
                                        <small class="text-muted">{{ $application->user->email }}</small>
                                        @if($application->user->job_title)
                                            <br>
                                            <small class="text-info">{{ $application->user->job_title }}</small>
                                        @endif
                                    </div>
                                </div>
                            </td>
                            <td>
                                <strong>{{ $application->job->title }}</strong>
                                <br>
                                <small class="text-muted">{{ $application->job->location }}</small>
                            </td>
                            <td>
                                <span class="badge bg-{{ 
                                    $application->status === 'applied' ? 'primary' : 
                                    ($application->status === 'under_review' ? 'warning' : 
                                    ($application->status === 'shortlisted' ? 'success' : 
                                    ($application->status === 'hired' ? 'info' : 'secondary'))) 
                                }}">
                                    {{ ucfirst(str_replace('_', ' ', $application->status)) }}
                                </span>
                            </td>
                            <td>
                                {{ $application->created_at->format('M d, Y') }}
                                <br>
                                <small class="text-muted">{{ $application->created_at->diffForHumans() }}</small>
                            </td>
                            <td>
                                <div class="btn-group">
                                    <a href="{{ route('recruiter.applications.show', $application) }}" 
                                       class="btn btn-sm btn-outline-primary">
                                        <i class="fas fa-eye me-1"></i>View
                                    </a>
                                    @if($application->resume_path)
                                        <a href="{{ route('recruiter.applications.download-resume', $application) }}" 
                                           class="btn btn-sm btn-outline-success">
                                            <i class="fas fa-download me-1"></i>Resume
                                        </a>
                                    @endif
                                </div>
                            </td>
                        </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <div class="d-flex justify-content-center mt-4">
                {{ $applications->links() }}
            </div>
        @else
            <div class="text-center py-5">
                <i class="fas fa-file-alt fa-3x text-muted mb-3"></i>
                <h4>No Applications Found</h4>
                <p class="text-muted">No applications match your current filters.</p>
            </div>
        @endif
    </div>
</div>
@endsection

@section('scripts')
<script>
function toggleAll() {
    const selectAll = document.getElementById('select-all');
    const checkboxes = document.querySelectorAll('.application-checkbox');
    
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAll.checked;
    });
    
    updateSelectedCount();
}

function updateSelectedCount() {
    const checkboxes = document.querySelectorAll('.application-checkbox:checked');
    document.getElementById('selected-count').textContent = checkboxes.length;
}

function confirmBulkUpdate() {
    const selected = document.querySelectorAll('.application-checkbox:checked');
    if (selected.length === 0) {
        alert('Please select at least one application.');
        return false;
    }
    
    return confirm(`Are you sure you want to update ${selected.length} application(s)?`);
}
</script>
@endsection