@extends('layouts.app')

@section('title', 'App Version Details')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>App Version Details</h2>
        <div>
            <a href="{{ route('admin.app-versions.edit', $version->id) }}" class="btn btn-primary me-2">
                <i class="fas fa-edit me-1"></i> Edit
            </a>
            <a href="{{ route('admin.app-versions.index') }}" class="btn btn-secondary">
                <i class="fas fa-arrow-left me-1"></i> Back to List
            </a>
        </div>
    </div>
    
    <div class="row">
        <div class="col-md-8">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Version Information</h6>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <strong>Platform:</strong>
                            <span class="badge bg-{{ $version->platform == 'android' ? 'success' : 'primary' }} ms-2">
                                {{ ucfirst($version->platform) }}
                            </span>
                        </div>
                        <div class="col-md-6 mb-3">
                            <strong>Status:</strong>
                            @if($version->is_active)
                                <span class="badge bg-success ms-2">Active</span>
                            @else
                                <span class="badge bg-secondary ms-2">Inactive</span>
                            @endif
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <strong>Version Name:</strong>
                            <span class="ms-2">{{ $version->version_name }}</span>
                        </div>
                        <div class="col-md-6 mb-3">
                            <strong>Version Code:</strong>
                            <span class="ms-2">{{ $version->version_code }}</span>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <strong>Minimum Version Name:</strong>
                            <span class="ms-2">{{ $version->minimum_version_name ?: 'Not set' }}</span>
                        </div>
                        <div class="col-md-6 mb-3">
                            <strong>Minimum Version Code:</strong>
                            <span class="ms-2">{{ $version->minimum_version_code ?: 'Not set' }}</span>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <strong>Force Update:</strong>
                            @if($version->force_update)
                                <span class="badge bg-danger ms-2">Yes</span>
                            @else
                                <span class="badge bg-secondary ms-2">No</span>
                            @endif
                        </div>
                        <div class="col-md-6 mb-3">
                            <strong>Created:</strong>
                            <span class="ms-2">{{ $version->created_at->format('M d, Y H:i A') }}</span>
                        </div>
                    </div>
                    
                    @if($version->download_url)
                        <div class="mb-3">
                            <strong>Download URL:</strong>
                            <div class="mt-1">
                                <a href="{{ $version->download_url }}" target="_blank" class="btn btn-sm btn-outline-primary">
                                    <i class="fas fa-external-link-alt me-1"></i> {{ $version->download_url }}
                                </a>
                            </div>
                        </div>
                    @endif
                    
                    @if($version->update_message)
                        <div class="mb-3">
                            <strong>Update Message:</strong>
                            <div class="mt-1 p-3 bg-light rounded">
                                {{ $version->update_message }}
                            </div>
                        </div>
                    @endif
                </div>
            </div>
        </div>
        
        <div class="col-md-4">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Actions</h6>
                </div>
                <div class="card-body">
                    <div class="d-grid gap-2">
                        <a href="{{ route('admin.app-versions.edit', $version->id) }}" class="btn btn-primary">
                            <i class="fas fa-edit me-1"></i> Edit Version
                        </a>
                        
                        <form action="{{ route('admin.app-versions.toggle-status', $version->id) }}" method="POST">
                            @csrf
                            <button type="submit" class="btn btn-{{ $version->is_active ? 'warning' : 'success' }} w-100"
                                    onclick="return confirm('Are you sure you want to {{ $version->is_active ? 'deactivate' : 'activate' }} this version?')">
                                <i class="fas fa-{{ $version->is_active ? 'pause' : 'play' }} me-1"></i>
                                {{ $version->is_active ? 'Deactivate' : 'Activate' }} Version
                            </button>
                        </form>
                        
                        <form action="{{ route('admin.app-versions.destroy', $version->id) }}" method="POST">
                            @csrf
                            @method('DELETE')
                            <button type="submit" class="btn btn-danger w-100"
                                    onclick="return confirm('Are you sure you want to delete this version? This action cannot be undone.')">
                                <i class="fas fa-trash me-1"></i> Delete Version
                            </button>
                        </form>
                    </div>
                </div>
            </div>
            
            @if($version->updated_at != $version->created_at)
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Last Updated</h6>
                    </div>
                    <div class="card-body">
                        <p class="mb-0">{{ $version->updated_at->format('M d, Y H:i A') }}</p>
                        <small class="text-muted">{{ $version->updated_at->diffForHumans() }}</small>
                    </div>
                </div>
            @endif
        </div>
    </div>
</div>
@endsection