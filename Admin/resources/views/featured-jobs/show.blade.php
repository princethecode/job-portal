@extends('layouts.app')

@section('content')
<div class="container">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Featured Job Details</h2>
        <div>
            <a href="{{ route('featured-jobs.edit', $featuredJob) }}" class="btn btn-warning">
                <i class="fas fa-edit me-1"></i> Edit Job
            </a>
            <a href="{{ route('featured-jobs.index') }}" class="btn btn-secondary ms-2">
                <i class="fas fa-arrow-left me-1"></i> Back to Featured Jobs
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
                        <h3>{{ $featuredJob->job_title }}</h3>
                        <div class="d-flex flex-wrap mb-2">
                            <span class="badge bg-primary me-2 mb-1">{{ $featuredJob->job_type }}</span>
                            <span class="badge bg-warning me-2 mb-1">Featured</span>
                            @if($featuredJob->is_active)
                                <span class="badge bg-success me-2 mb-1">Active</span>
                            @else
                                <span class="badge bg-danger me-2 mb-1">Inactive</span>
                            @endif
                        </div>
                        <div class="text-muted mb-3">
                            <p><i class="fas fa-building me-2"></i>{{ $featuredJob->company_name }}</p>
                            <p><i class="fas fa-map-marker-alt me-2"></i>{{ $featuredJob->location }}</p>
                            <p><i class="fas fa-money-bill-wave me-2"></i>{{ $featuredJob->salary }}</p>
                            <p><i class="fas fa-calendar-alt me-2"></i>Posted: {{ $featuredJob->posted_date->format('M d, Y') }}</p>
                        </div>
                    </div>
                    
                    @if(!empty($featuredJob->company_logo))
                        <div class="mb-4">
                            <h5>Company Logo</h5>
                            <img src="{{ asset('' . $featuredJob->company_logo) }}" alt="Company Logo" style="max-width: 150px; max-height: 150px; border-radius: 10px; border: 1px solid #ccc; box-shadow: 0 2px 8px rgba(0,0,0,0.07);">
                        </div>
                    @endif
                    
                    @if(!empty($featuredJob->job_image))
                        <div class="mb-4">
                            <h5>Job Image</h5>
                            <img src="{{ asset('' . $featuredJob->job_image) }}" alt="Job Image" style="max-width: 100%; max-height: 400px; border-radius: 10px; border: 1px solid #ccc; box-shadow: 0 2px 8px rgba(0,0,0,0.07);">
                        </div>
                    @endif
                    
                    <div class="mb-3">
                        <h5>Job Description</h5>
                        <div class="p-3 bg-light rounded">
                            {!! nl2br(e($featuredJob->description)) !!}
                        </div>
                    </div>

                    @if(!empty($featuredJob->requirements))
                    <div class="mb-3">
                        <h5><i class="fas fa-clipboard-list me-2"></i>Requirements</h5>
                        <div class="p-3 bg-light rounded">
                            {!! nl2br(e($featuredJob->requirements)) !!}
                        </div>
                    </div>
                    @endif

                    @if(!empty($featuredJob->benefits))
                    <div class="mb-3">
                        <h5><i class="fas fa-gift me-2"></i>Benefits</h5>
                        <div class="p-3 bg-light rounded">
                            {!! nl2br(e($featuredJob->benefits)) !!}
                        </div>
                    </div>
                    @endif

                    @if(!empty($featuredJob->skills_required))
                    <div class="mb-3">
                        <h5><i class="fas fa-tools me-2"></i>Required Skills</h5>
                        <div class="p-3 bg-light rounded">
                            @php
                                $skills = is_array($featuredJob->skills_required) ? $featuredJob->skills_required : json_decode($featuredJob->skills_required, true);
                                if (!is_array($skills)) {
                                    $skills = explode(',', $featuredJob->skills_required);
                                }
                            @endphp
                            @foreach($skills as $skill)
                                <span class="badge bg-primary me-2 mb-2">{{ trim($skill) }}</span>
                            @endforeach
                        </div>
                    </div>
                    @endif
                </div>
            </div>
        </div>
        
        <div class="col-md-4">
            <!-- Quick Actions Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Quick Actions</h6>
                </div>
                <div class="card-body">
                    <div class="d-grid gap-2">
                        <a href="{{ route('featured-jobs.edit', $featuredJob) }}" class="btn btn-warning">
                            <i class="fas fa-edit me-1"></i> Edit Job
                        </a>
                        
                        <form action="{{ route('featured-jobs.destroy', $featuredJob) }}" method="POST" onsubmit="return confirm('Are you sure you want to delete this featured job?');">
                            @csrf
                            @method('DELETE')
                            <button type="submit" class="btn btn-danger w-100">
                                <i class="fas fa-trash me-1"></i> Delete Job
                            </button>
                        </form>
                        
                        <a href="{{ route('featured-jobs.index') }}" class="btn btn-secondary">
                            <i class="fas fa-arrow-left me-1"></i> Back to List
                        </a>
                    </div>
                </div>
            </div>

            <!-- Job Statistics Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Job Statistics</h6>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-muted">Status:</span>
                            <span class="badge {{ $featuredJob->is_active ? 'bg-success' : 'bg-danger' }}">
                                {{ $featuredJob->is_active ? 'Active' : 'Inactive' }}
                            </span>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-muted">Posted Date:</span>
                            <span>{{ $featuredJob->posted_date->format('M d, Y') }}</span>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-muted">Created:</span>
                            <span>{{ $featuredJob->created_at->format('M d, Y') }}</span>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-muted">Last Updated:</span>
                            <span>{{ $featuredJob->updated_at->format('M d, Y') }}</span>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-muted">Job Type:</span>
                            <span class="badge bg-primary">{{ $featuredJob->job_type }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
