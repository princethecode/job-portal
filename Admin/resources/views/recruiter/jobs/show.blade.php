@extends('recruiter.layouts.app')

@section('title', $job->title)
@section('page-title', $job->title)

@section('content')
<div class="row">
    <div class="col-lg-8">
        <div class="card shadow mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Job Details</h5>
                <div>
                    <span class="badge bg-{{ $job->is_active ? 'success' : 'secondary' }} me-2">
                        {{ $job->is_active ? 'Active' : 'Inactive' }}
                    </span>
                    <div class="btn-group">
                        <a href="{{ route('recruiter.jobs.edit', $job) }}" class="btn btn-sm btn-outline-primary">
                            <i class="fas fa-edit me-1"></i>Edit
                        </a>
                        <form method="POST" action="{{ route('recruiter.jobs.toggle-status', $job) }}" class="d-inline">
                            @csrf
                            <button type="submit" class="btn btn-sm btn-outline-{{ $job->is_active ? 'warning' : 'success' }}">
                                <i class="fas fa-{{ $job->is_active ? 'pause' : 'play' }} me-1"></i>
                                {{ $job->is_active ? 'Deactivate' : 'Activate' }}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
            <div class="card-body">
                @if($job->image)
                <div class="mb-3">
                    <img src="{{ asset('storage/' . $job->image) }}" alt="Job Image" class="img-fluid rounded" style="max-height: 200px;">
                </div>
                @endif

                <div class="row mb-3">
                    <div class="col-md-6">
                        <strong>Company:</strong> {{ $job->company }}
                    </div>
                    <div class="col-md-6">
                        <strong>Job Type:</strong> {{ $job->job_type }}
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <strong>Location:</strong> {{ $job->location }}
                    </div>
                    <div class="col-md-6">
                        <strong>Category:</strong> {{ $job->category }}
                    </div>
                </div>

                <div class="row mb-3">
                    @if($job->salary)
                    <div class="col-md-6">
                        <strong>Salary:</strong> ${{ number_format($job->salary) }}
                    </div>
                    @endif
                    @if($job->experience_required)
                    <div class="col-md-6">
                        <strong>Experience:</strong> {{ $job->experience_required }}
                    </div>
                    @endif
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <strong>Posted:</strong> {{ $job->posting_date->format('M d, Y') }}
                    </div>
                    <div class="col-md-6">
                        <strong>Deadline:</strong> {{ $job->expiry_date->format('M d, Y') }}
                    </div>
                </div>

                <hr>

                <h6>Description</h6>
                <p>{{ $job->description }}</p>

                @if($job->requirements)
                <h6>Requirements</h6>
                <p>{{ $job->requirements }}</p>
                @endif

                @if($job->benefits)
                <h6>Benefits</h6>
                <p>{{ $job->benefits }}</p>
                @endif

                @if($job->skills_required)
                <h6>Required Skills</h6>
                <div class="mb-3">
                    @foreach(json_decode($job->skills_required, true) ?? [] as $skill)
                        <span class="badge bg-primary me-2">{{ $skill }}</span>
                    @endforeach
                </div>
                @endif
            </div>
        </div>

        <!-- Applications Section -->
        <div class="card shadow">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Applications ({{ $job->applications->count() }})</h5>
                <a href="{{ route('recruiter.applications.index', ['job_id' => $job->id]) }}" class="btn btn-sm btn-primary">
                    View All Applications
                </a>
            </div>
            <div class="card-body">
                @if($job->applications->count() > 0)
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>Candidate</th>
                                    <th>Status</th>
                                    <th>Applied Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                @foreach($job->applications->take(5) as $application)
                                <tr>
                                    <td>
                                        <div>
                                            <strong>{{ $application->user->name }}</strong>
                                            <br>
                                            <small class="text-muted">{{ $application->user->email }}</small>
                                        </div>
                                    </td>
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
                    <p class="text-muted text-center">No applications received yet.</p>
                @endif
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <!-- Job Statistics -->
        <div class="card shadow mb-4">
            <div class="card-header">
                <h6 class="mb-0">Job Statistics</h6>
            </div>
            <div class="card-body">
                <div class="row text-center">
                    <div class="col-6 mb-3">
                        <div class="border-end">
                            <h4 class="text-primary">{{ $job->views_count }}</h4>
                            <small class="text-muted">Views</small>
                        </div>
                    </div>
                    <div class="col-6 mb-3">
                        <h4 class="text-success">{{ $job->applications->count() }}</h4>
                        <small class="text-muted">Applications</small>
                    </div>
                </div>
                
                <hr>
                
                <div class="mb-2">
                    <small class="text-muted">Application Status Breakdown:</small>
                </div>
                
                @php
                    $statusCounts = $job->applications->groupBy('status')->map->count();
                @endphp
                
                @foreach(['applied', 'under_review', 'shortlisted', 'rejected'] as $status)
                    @if($statusCounts->get($status, 0) > 0)
                    <div class="d-flex justify-content-between mb-1">
                        <span class="small">{{ ucfirst(str_replace('_', ' ', $status)) }}</span>
                        <span class="badge bg-secondary">{{ $statusCounts->get($status, 0) }}</span>
                    </div>
                    @endif
                @endforeach
            </div>
        </div>

        <!-- Recent Interviews -->
        @if($job->interviews->count() > 0)
        <div class="card shadow">
            <div class="card-header">
                <h6 class="mb-0">Recent Interviews</h6>
            </div>
            <div class="card-body">
                @foreach($job->interviews->take(3) as $interview)
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <div>
                        <strong>{{ $interview->user->name }}</strong>
                        <br>
                        <small class="text-muted">{{ $interview->interview_date->format('M d, Y') }}</small>
                    </div>
                    <span class="badge bg-{{ $interview->status === 'scheduled' ? 'warning' : ($interview->status === 'completed' ? 'success' : 'secondary') }}">
                        {{ ucfirst($interview->status) }}
                    </span>
                </div>
                @if(!$loop->last)<hr>@endif
                @endforeach
                
                <div class="text-center mt-3">
                    <a href="{{ route('recruiter.interviews.index', ['job_id' => $job->id]) }}" class="btn btn-sm btn-outline-primary">
                        View All Interviews
                    </a>
                </div>
            </div>
        </div>
        @endif
    </div>
</div>
@endsection