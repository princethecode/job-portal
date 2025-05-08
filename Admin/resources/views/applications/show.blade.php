@extends('layouts.app')

@section('title', 'Application Details')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Application Details</h2>
        <a href="{{ route('admin.applications.index') }}" class="btn btn-secondary">
            <i class="fas fa-arrow-left me-1"></i> Back to Applications
        </a>
    </div>
    
    <div class="row">
        <div class="col-md-8">
            <!-- Application Details Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex justify-content-between align-items-center">
                    <h6 class="m-0 font-weight-bold text-primary">Application Information</h6>
                    <div>
                        @if($application['status'] == 'Applied')
                            <span class="badge bg-primary">Applied</span>
                        @elseif($application['status'] == 'Under Review')
                            <span class="badge bg-info">Under Review</span>
                        @elseif($application['status'] == 'Shortlisted')
                            <span class="badge bg-success">Shortlisted</span>
                        @elseif($application['status'] == 'Rejected')
                            <span class="badge bg-danger">Rejected</span>
                        @endif
                    </div>
                </div>
                <div class="card-body">
                    <div class="mb-4">
                        <h4>{{ $application['job']['title'] }}</h4>
                        <p class="text-muted">
                            <i class="fas fa-building me-2"></i>{{ $application['job']['company'] }}
                        </p>
                        <p class="text-muted">
                            <i class="fas fa-map-marker-alt me-2"></i>{{ $application['job']['location'] }}
                        </p>
                        <p class="text-muted">
                            <i class="fas fa-calendar-alt me-2"></i>Applied on: {{ \Carbon\Carbon::parse($application['applied_date'])->format('M d, Y') }}
                        </p>
                    </div>
                    
                    <div class="mb-4">
                        <h5>Update Application Status</h5>
                        <form action="{{ route('admin.applications.update-status', $application['id']) }}" method="POST" class="mt-3">
                            @csrf
                            @method('PUT')
                            <div class="row align-items-end">
                                <div class="col-md-8 mb-3">
                                    <label for="status" class="form-label">Status</label>
                                    <select class="form-select" id="status" name="status" required>
                                        <option value="Applied" {{ $application['status'] == 'Applied' ? 'selected' : '' }}>Applied</option>
                                        <option value="Under Review" {{ $application['status'] == 'Under Review' ? 'selected' : '' }}>Under Review</option>
                                        <option value="Shortlisted" {{ $application['status'] == 'Shortlisted' ? 'selected' : '' }}>Shortlisted</option>
                                        <option value="Rejected" {{ $application['status'] == 'Rejected' ? 'selected' : '' }}>Rejected</option>
                                    </select>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <button type="submit" class="btn btn-primary w-100">
                                        <i class="fas fa-save me-1"></i> Update Status
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                    
                    @if($application['resume_path'])
                    <div class="mb-4">
                        <h5>Resume</h5>
                        <div class="mt-2">
                            <a href="{{ env('API_URL') . '/storage/' . $application['resume_path'] }}" target="_blank" class="btn btn-outline-primary">
                                <i class="fas fa-file-pdf me-1"></i> View Resume
                            </a>
                            <a href="{{ env('API_URL') . '/storage/' . $application['resume_path'] }}" download class="btn btn-outline-secondary ms-2">
                                <i class="fas fa-download me-1"></i> Download Resume
                            </a>
                        </div>
                    </div>
                    @endif
                </div>
            </div>
        </div>
        
        <div class="col-md-4">
            <!-- Applicant Details Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Applicant Information</h6>
                </div>
                <div class="card-body">
                    <div class="text-center mb-3">
                        <i class="fas fa-user-circle fa-5x text-gray-300"></i>
                        <h5 class="mt-3">{{ $application['user']['name'] }}</h5>
                    </div>
                    
                    <div class="mb-3">
                        <p><i class="fas fa-envelope me-2"></i>{{ $application['user']['email'] }}</p>
                        @if($application['user']['mobile'])
                            <p><i class="fas fa-phone me-2"></i>{{ $application['user']['mobile'] }}</p>
                        @endif
                    </div>
                    
                    @if($application['user']['skills'])
                    <div class="mb-3">
                        <h6>Skills</h6>
                        <div class="p-2 bg-light rounded">
                            {!! nl2br(e($application['user']['skills'])) !!}
                        </div>
                    </div>
                    @endif
                    
                    @if($application['user']['experience'])
                    <div class="mb-3">
                        <h6>Experience</h6>
                        <div class="p-2 bg-light rounded">
                            {!! nl2br(e($application['user']['experience'])) !!}
                        </div>
                    </div>
                    @endif
                    
                    <div class="d-grid gap-2">
                        <a href="{{ route('admin.users.show', $application['user']['id']) }}" class="btn btn-info">
                            <i class="fas fa-user me-1"></i> View Full Profile
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
