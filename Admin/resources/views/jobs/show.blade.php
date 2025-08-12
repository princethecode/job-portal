@extends('layouts.app')

@section('title', 'Job Details')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Job Details</h2>
        <div>
            <a href="{{ route('admin.jobs.edit', $job['id']) }}" class="btn btn-warning">
                <i class="fas fa-edit me-1"></i> Edit Job
            </a>
            <a href="{{ route('admin.jobs.index') }}" class="btn btn-secondary ms-2">
                <i class="fas fa-arrow-left me-1"></i> Back to Jobs
            </a>
        </div>
    </div>
    
    <div class="row">
        <div class="col-md-8">
            <!-- Job Details Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Job Information</h6>
                </div>
                <div class="card-body">
                    <div class="mb-4">
                        <h3>{{ $job['title'] }}</h3>
                        <div class="d-flex flex-wrap mb-2">
                            <span class="badge bg-primary me-2 mb-1">{{ $job['job_type'] }}</span>
                            <span class="badge bg-secondary me-2 mb-1">{{ $job['category'] }}</span>
                            @if($job['is_active'])
                                <span class="badge bg-success me-2 mb-1">Active</span>
                            @else
                                <span class="badge bg-danger me-2 mb-1">Inactive</span>
                            @endif
                        </div>
                        <div class="text-muted mb-3">
                            <p><i class="fas fa-building me-2"></i>{{ $job['company'] }}</p>
                            <p><i class="fas fa-map-marker-alt me-2"></i>{{ $job['location'] }}</p>
                            @if($job['salary'])
                                <p><i class="fas fa-money-bill-wave me-2"></i>{{ $job['salary'] }}</p>
                            @endif
                            <p><i class="fas fa-calendar-alt me-2"></i>Posted: {{ \Carbon\Carbon::parse($job['posting_date'])->format('M d, Y') }}</p>
                            <p><i class="fas fa-calendar-times me-2"></i>Expires: {{ \Carbon\Carbon::parse($job['expiry_date'])->format('M d, Y') }}</p>
                        </div>
                    </div>
                    
                    @if(!empty($job['image']))
                        <div class="mb-4 text-center">
                            <img src="{{ asset('/' . $job['image']) }}" alt="Job Image" style="max-width: 350px; max-height: 220px; border-radius: 10px; border: 1px solid #ccc; box-shadow: 0 2px 8px rgba(0,0,0,0.07);">
                        </div>
                    @endif
                    <div class="mb-3">
                        <h5>Job Description</h5>
                        <div class="p-3 bg-light rounded">
                            {!! nl2br(e($job['description'])) !!}
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="col-md-4">
            <!-- Applications Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Applications</h6>
                </div>
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5>Total Applications</h5>
                        <span class="badge bg-primary rounded-pill">{{ isset($applications['data']) ? count($applications['data']) : 0 }}</span>
                    </div>
                    
                    <div class="list-group">
                        @if(isset($applications['data']) && count($applications['data']) > 0)
                            @foreach($applications['data'] as $application)
                                <a href="{{ route('admin.applications.show', $application['id']) }}" class="list-group-item list-group-item-action">
                                    <div class="d-flex w-100 justify-content-between">
                                        <h6 class="mb-1">{{ $application['user']['name'] }}</h6>
                                        <small>{{ \Carbon\Carbon::parse($application['applied_date'])->format('M d, Y') }}</small>
                                    </div>
                                    <p class="mb-1">{{ $application['user']['email'] }}</p>
                                    <small>
                                        @if($application['status'] == 'Applied')
                                            <span class="badge bg-primary">Applied</span>
                                        @elseif($application['status'] == 'Under Review')
                                            <span class="badge bg-info">Under Review</span>
                                        @elseif($application['status'] == 'Shortlisted')
                                            <span class="badge bg-success">Shortlisted</span>
                                        @elseif($application['status'] == 'Rejected')
                                            <span class="badge bg-danger">Rejected</span>
                                        @endif
                                    </small>
                                </a>
                            @endforeach
                        @else
                            <div class="text-center py-3">
                                <p class="text-muted">No applications yet</p>
                            </div>
                        @endif
                    </div>
                    
                    @if(isset($applications['data']) && count($applications['data']) > 0)
                        <div class="d-grid gap-2 mt-3">
                            <a href="{{ route('admin.applications.index', ['job_id' => $job['id']]) }}" class="btn btn-primary">
                                View All Applications
                            </a>
                        </div>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
