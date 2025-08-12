@extends('layouts.app')

@section('content')
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h4 class="mb-0">Edit Featured Job</h4>
                </div>
                <div class="card-body">
                    <form action="{{ route('featured-jobs.update', $featuredJob) }}" method="POST" enctype="multipart/form-data">
                        @csrf
                        @method('PUT')
                        
                        <div class="mb-3">
                            <label for="company_logo" class="form-label">Company Logo</label>
                            @if($featuredJob->company_logo)
                                <div class="mb-2">
                                    <img src="{{ asset('' . $featuredJob->company_logo) }}" alt="Current Logo" class="img-thumbnail" style="max-width: 100px;">
                                </div>
                            @endif
                            <input type="file" class="form-control @error('company_logo') is-invalid @enderror" 
                                   id="company_logo" name="company_logo" accept="image/*">
                            <div class="form-text">Upload a new logo (JPEG, PNG, JPG, GIF up to 2MB)</div>
                            @error('company_logo')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <label for="job_title" class="form-label">Job Title</label>
                            <input type="text" class="form-control @error('job_title') is-invalid @enderror" 
                                   id="job_title" name="job_title" value="{{ old('job_title', $featuredJob->job_title) }}" required>
                            @error('job_title')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <label for="company_name" class="form-label">Company Name</label>
                            <input type="text" class="form-control @error('company_name') is-invalid @enderror" 
                                   id="company_name" name="company_name" value="{{ old('company_name', $featuredJob->company_name) }}" required>
                            @error('company_name')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <label for="location" class="form-label">Location</label>
                            <input type="text" class="form-control @error('location') is-invalid @enderror" 
                                   id="location" name="location" value="{{ old('location', $featuredJob->location) }}" required>
                            @error('location')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <label for="salary" class="form-label">Salary</label>
                            <input type="text" class="form-control @error('salary') is-invalid @enderror" 
                                   id="salary" name="salary" value="{{ old('salary', $featuredJob->salary) }}" required>
                            @error('salary')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <label for="job_type" class="form-label">Job Type</label>
                            <select class="form-select @error('job_type') is-invalid @enderror" 
                                    id="job_type" name="job_type" required>
                                <option value="">Select Job Type</option>
                                <option value="Full-time" {{ old('job_type', $featuredJob->job_type) == 'Full-time' ? 'selected' : '' }}>Full-time</option>
                                <option value="Part-time" {{ old('job_type', $featuredJob->job_type) == 'Part-time' ? 'selected' : '' }}>Part-time</option>
                                <option value="Contract" {{ old('job_type', $featuredJob->job_type) == 'Contract' ? 'selected' : '' }}>Contract</option>
                                <option value="Freelance" {{ old('job_type', $featuredJob->job_type) == 'Freelance' ? 'selected' : '' }}>Freelance</option>
                            </select>
                            @error('job_type')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <label for="description" class="form-label">Description</label>
                            <textarea class="form-control @error('description') is-invalid @enderror" 
                                      id="description" name="description" rows="5" required>{{ old('description', $featuredJob->description) }}</textarea>
                            @error('description')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>

                        <div class="mb-3">
                            <div class="form-check">
                                <input type="checkbox" class="form-check-input @error('is_active') is-invalid @enderror" 
                                       id="is_active" name="is_active" value="1" {{ old('is_active', $featuredJob->is_active) ? 'checked' : '' }}>
                                <label class="form-check-label" for="is_active">Active</label>
                                @error('is_active')
                                    <div class="invalid-feedback">{{ $message }}</div>
                                @enderror
                            </div>
                        </div>

                        <div class="d-flex justify-content-between">
                            <a href="{{ route('featured-jobs.index') }}" class="btn btn-secondary">Cancel</a>
                            <button type="submit" class="btn btn-primary">Update Featured Job</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection 