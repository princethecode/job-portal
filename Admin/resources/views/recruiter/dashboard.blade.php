@extends('recruiter.layouts.app')

@section('title', 'Recruiter Dashboard')
@section('page-title', 'Dashboard')

@section('content')
<div class="row">
    <!-- Statistics Cards -->
    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-primary shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                            Total Jobs Posted
                        </div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $totalJobs }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-briefcase fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-success shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                            Approved Jobs
                        </div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $approvedJobs }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-check-circle fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-warning shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                            Pending Approval
                        </div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $pendingApprovalJobs }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-clock fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-info shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                            Total Applications
                        </div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $totalApplications }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-file-alt fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Job Approval Status Row -->
@if($pendingApprovalJobs > 0 || $declinedJobs > 0)
<div class="row mb-4">
    @if($pendingApprovalJobs > 0)
    <div class="col-md-6 mb-3">
        <div class="alert alert-warning">
            <div class="d-flex align-items-center">
                <i class="fas fa-clock fa-2x me-3"></i>
                <div>
                    <h5 class="alert-heading mb-1">{{ $pendingApprovalJobs }} Job(s) Pending Approval</h5>
                    <p class="mb-0">Your job postings are being reviewed by admin. You'll receive an email notification once they're approved.</p>
                </div>
            </div>
        </div>
    </div>
    @endif
    
    @if($declinedJobs > 0)
    <div class="col-md-6 mb-3">
        <div class="alert alert-danger">
            <div class="d-flex align-items-center">
                <i class="fas fa-times-circle fa-2x me-3"></i>
                <div>
                    <h5 class="alert-heading mb-1">{{ $declinedJobs }} Job(s) Declined</h5>
                    <p class="mb-0">Some of your job postings were declined. Please check your email for feedback and resubmit.</p>
                </div>
            </div>
        </div>
    </div>
    @endif
</div>
@endif

<div class="row">
    <!-- Recent Applications -->
    <div class="col-lg-8 mb-4">
        <div class="card shadow">
            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                <h6 class="m-0 font-weight-bold text-primary">Recent Applications</h6>
                <a href="{{ route('recruiter.applications.index') }}" class="btn btn-sm btn-primary">View All</a>
            </div>
            <div class="card-body">
                @if($recentApplications->count() > 0)
                    <div class="table-responsive">
                        <table class="table table-bordered">
                            <thead>
                                <tr>
                                    <th>Candidate</th>
                                    <th>Job</th>
                                    <th>Status</th>
                                    <th>Applied Date</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                @foreach($recentApplications as $application)
                                <tr>
                                    <td>{{ $application->user->name }}</td>
                                    <td>{{ $application->job->title }}</td>
                                    <td>
                                        <span class="badge bg-{{ $application->status === 'applied' ? 'primary' : ($application->status === 'shortlisted' ? 'success' : 'secondary') }}">
                                            {{ ucfirst($application->status) }}
                                        </span>
                                    </td>
                                    <td>{{ $application->created_at->format('M d, Y') }}</td>
                                    <td>
                                        <a href="{{ route('recruiter.applications.show', $application) }}" class="btn btn-sm btn-outline-primary">
                                            View
                                        </a>
                                    </td>
                                </tr>
                                @endforeach
                            </tbody>
                        </table>
                    </div>
                @else
                    <p class="text-muted text-center">No applications yet.</p>
                @endif
            </div>
        </div>
    </div>

    <!-- Job Performance -->
    <div class="col-lg-4 mb-4">
        <div class="card shadow">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Top Performing Jobs</h6>
            </div>
            <div class="card-body">
                @if($jobStats->count() > 0)
                    @foreach($jobStats as $job)
                    <div class="mb-3">
                        <div class="d-flex justify-content-between">
                            <span class="small">{{ Str::limit($job->title, 30) }}</span>
                            <span class="badge bg-primary">{{ $job->applications_count }} applications</span>
                        </div>
                        <div class="progress mt-1" style="height: 5px;">
                            <div class="progress-bar" role="progressbar" 
                                 style="width: {{ $job->applications_count > 0 ? ($job->applications_count / $jobStats->max('applications_count')) * 100 : 0 }}%">
                            </div>
                        </div>
                    </div>
                    @endforeach
                @else
                    <p class="text-muted text-center">No jobs posted yet.</p>
                @endif
            </div>
        </div>
    </div>
</div>

<!-- Quick Actions -->
<div class="row">
    <div class="col-12">
        <div class="card shadow">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Quick Actions</h6>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <a href="{{ route('recruiter.jobs.create') }}" class="btn btn-primary w-100">
                            <i class="fas fa-plus me-2"></i>
                            Post New Job
                        </a>
                    </div>
                    <div class="col-md-3 mb-3">
                        <a href="{{ route('recruiter.applications.index') }}" class="btn btn-info w-100">
                            <i class="fas fa-file-alt me-2"></i>
                            Review Applications
                        </a>
                    </div>
                    <div class="col-md-3 mb-3">
                        <a href="{{ route('recruiter.candidates.index') }}" class="btn btn-success w-100">
                            <i class="fas fa-search me-2"></i>
                            Search Candidates
                        </a>
                    </div>
                    <div class="col-md-3 mb-3">
                        <a href="{{ route('recruiter.interviews.calendar') }}" class="btn btn-warning w-100">
                            <i class="fas fa-calendar me-2"></i>
                            Interview Calendar
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<style>
.border-left-primary {
    border-left: 0.25rem solid #4e73df !important;
}
.border-left-success {
    border-left: 0.25rem solid #1cc88a !important;
}
.border-left-info {
    border-left: 0.25rem solid #36b9cc !important;
}
.border-left-warning {
    border-left: 0.25rem solid #f6c23e !important;
}
</style>
@endsection