@extends('layouts.app')

@section('title', 'Edit App Version')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Edit App Version</h2>
        <div>
            <a href="{{ route('admin.app-versions.show', $version->id) }}" class="btn btn-info me-2">
                <i class="fas fa-eye me-1"></i> View Details
            </a>
            <a href="{{ route('admin.app-versions.index') }}" class="btn btn-secondary">
                <i class="fas fa-arrow-left me-1"></i> Back to List
            </a>
        </div>
    </div>
    
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Edit App Version Details</h6>
        </div>
        <div class="card-body">
            <form action="{{ route('admin.app-versions.update', $version->id) }}" method="POST">
                @csrf
                @method('PUT')
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="platform" class="form-label">Platform <span class="text-danger">*</span></label>
                        <select class="form-select @error('platform') is-invalid @enderror" id="platform" name="platform" required>
                            <option value="">Select Platform</option>
                            <option value="android" {{ old('platform', $version->platform) == 'android' ? 'selected' : '' }}>Android</option>
                            <option value="ios" {{ old('platform', $version->platform) == 'ios' ? 'selected' : '' }}>iOS</option>
                        </select>
                        @error('platform')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="version_name" class="form-label">Version Name <span class="text-danger">*</span></label>
                        <input type="text" class="form-control @error('version_name') is-invalid @enderror" 
                               id="version_name" name="version_name" value="{{ old('version_name', $version->version_name) }}" 
                               placeholder="e.g., 1.0.0" required>
                        @error('version_name')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="version_code" class="form-label">Version Code <span class="text-danger">*</span></label>
                        <input type="number" class="form-control @error('version_code') is-invalid @enderror" 
                               id="version_code" name="version_code" value="{{ old('version_code', $version->version_code) }}" 
                               placeholder="e.g., 10" min="1" required>
                        @error('version_code')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="minimum_version_name" class="form-label">Minimum Version Name</label>
                        <input type="text" class="form-control @error('minimum_version_name') is-invalid @enderror" 
                               id="minimum_version_name" name="minimum_version_name" value="{{ old('minimum_version_name', $version->minimum_version_name) }}" 
                               placeholder="e.g., 1.0.0">
                        @error('minimum_version_name')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="minimum_version_code" class="form-label">Minimum Version Code</label>
                        <input type="number" class="form-control @error('minimum_version_code') is-invalid @enderror" 
                               id="minimum_version_code" name="minimum_version_code" value="{{ old('minimum_version_code', $version->minimum_version_code) }}" 
                               placeholder="e.g., 10" min="1">
                        @error('minimum_version_code')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="download_url" class="form-label">Download URL</label>
                        <input type="url" class="form-control @error('download_url') is-invalid @enderror" 
                               id="download_url" name="download_url" value="{{ old('download_url', $version->download_url) }}" 
                               placeholder="https://example.com/app.apk">
                        @error('download_url')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>
                </div>
                
                <div class="mb-3">
                    <label for="update_message" class="form-label">Update Message</label>
                    <textarea class="form-control @error('update_message') is-invalid @enderror" 
                              id="update_message" name="update_message" rows="3" 
                              placeholder="What's new in this version...">{{ old('update_message', $version->update_message) }}</textarea>
                    @error('update_message')
                        <div class="invalid-feedback">{{ $message }}</div>
                    @enderror
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="force_update" name="force_update" value="1" 
                                   {{ old('force_update', $version->force_update) ? 'checked' : '' }}>
                            <label class="form-check-label" for="force_update">
                                Force Update Required
                            </label>
                        </div>
                        <small class="text-muted">Users will be forced to update to this version</small>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="is_active" name="is_active" value="1" 
                                   {{ old('is_active', $version->is_active) ? 'checked' : '' }}>
                            <label class="form-check-label" for="is_active">
                                Set as Active Version
                            </label>
                        </div>
                        <small class="text-muted">Only one version per platform can be active</small>
                    </div>
                </div>
                
                <div class="d-flex justify-content-end">
                    <a href="{{ route('admin.app-versions.show', $version->id) }}" class="btn btn-secondary me-2">Cancel</a>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-save me-1"></i> Update Version
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection