@extends('layouts.app')

@section('title', 'Edit Job')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Edit Job</h2>
        <a href="{{ route('admin.jobs.index') }}" class="btn btn-secondary">
            <i class="fas fa-arrow-left me-1"></i> Back to Jobs
        </a>
    </div>
    
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Job Details</h6>
        </div>
        <div class="card-body">
            @if($errors->any())
                <div class="alert alert-danger">
                    <ul class="mb-0">
                        @foreach($errors->all() as $error)
                            <li>{{ $error }}</li>
                        @endforeach
                    </ul>
                </div>
            @endif

            <form action="{{ route('admin.jobs.update', $job['id']) }}" method="POST" enctype="multipart/form-data">
                @csrf
                @method('PUT')
                @csrf
                @method('PUT')
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="title" class="form-label">Job Title</label>
                        <input type="text" class="form-control" id="title" name="title" value="{{ old('title', $job['title']) }}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="company" class="form-label">Company</label>
                        <input type="text" class="form-control" id="company" name="company" value="{{ old('company', $job['company']) }}" required>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="location" class="form-label">Location</label>
                        <input type="text" class="form-control" id="location" name="location" value="{{ old('location', $job['location']) }}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="salary" class="form-label">Salary (Optional)</label>
                        <input type="text" class="form-control" id="salary" name="salary" value="{{ old('salary', $job['salary']) }}">
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label for="job_type" class="form-label">Job Type</label>
                        <select class="form-select" id="job_type" name="job_type" required>
                            <option value="">Select Job Type</option>
                            <option value="Full-time" {{ old('job_type', $job['job_type']) == 'Full-time' ? 'selected' : '' }}>Full-time</option>
                            <option value="Part-time" {{ old('job_type', $job['job_type']) == 'Part-time' ? 'selected' : '' }}>Part-time</option>
                            <option value="Contract" {{ old('job_type', $job['job_type']) == 'Contract' ? 'selected' : '' }}>Contract</option>
                        </select>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label for="category" class="form-label">Category</label>
                        <input type="text" class="form-control" id="category" name="category" value="{{ old('category', $job['category']) }}" required>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label for="posting_date" class="form-label">Posting Date</label>
                        <input type="date" class="form-control" id="posting_date" name="posting_date" value="{{ old('posting_date', date('Y-m-d', strtotime($job['posting_date']))) }}" required>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="expiry_date" class="form-label">Expiry Date</label>
                        <input type="date" class="form-control" id="expiry_date" name="expiry_date" value="{{ old('expiry_date', date('Y-m-d', strtotime($job['expiry_date']))) }}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="is_active" class="form-label">Status</label>
                        <select class="form-select" id="is_active" name="is_active">
                            <option value="1" {{ old('is_active', $job['is_active']) ? 'selected' : '' }}>Active</option>
                            <option value="0" {{ old('is_active', $job['is_active']) ? '' : 'selected' }}>Inactive</option>
                        </select>
                    </div>
                </div>
                
                <div class="mb-3">
                    <label for="description" class="form-label">Job Description</label>
                    <textarea class="form-control" id="description" name="description" rows="6" required>{{ old('description', $job['description']) }}</textarea>
                </div>
                <div class="mb-3">
                    <label for="image" class="form-label">Job Image</label>
                    @if(!empty($job['image']))
                        <div class="mb-2">
                            <img src="{{ asset('storage/' . $job['image']) }}" alt="Current Job Image" style="max-width: 200px; max-height: 200px; border-radius: 8px; border: 1px solid #ccc;">
                        </div>
                    @endif
                    <input type="file" class="form-control-file" id="image" name="image" accept="image/*">
                    <small class="form-text text-muted">Upload a new image to replace the current one.</small>
                </div>
                
                <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-save me-1"></i> Update Job
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
