@extends('layouts.app')

@section('title', 'App Versions Management')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>App Versions Management</h2>
        <a href="{{ route('admin.app-versions.create') }}" class="btn btn-primary">
            <i class="fas fa-plus me-1"></i> Add New Version
        </a>
    </div>
    
    <!-- Filters -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Filter App Versions</h6>
        </div>
        <div class="card-body">
            <form action="{{ route('admin.app-versions.index') }}" method="GET">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="platform" class="form-label">Platform</label>
                        <select class="form-select" id="platform" name="platform">
                            <option value="">All Platforms</option>
                            <option value="android" {{ isset($filters['platform']) && $filters['platform'] == 'android' ? 'selected' : '' }}>Android</option>
                            <option value="ios" {{ isset($filters['platform']) && $filters['platform'] == 'ios' ? 'selected' : '' }}>iOS</option>
                        </select>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="is_active" class="form-label">Status</label>
                        <select class="form-select" id="is_active" name="is_active">
                            <option value="">All Statuses</option>
                            <option value="1" {{ isset($filters['is_active']) && $filters['is_active'] == '1' ? 'selected' : '' }}>Active</option>
                            <option value="0" {{ isset($filters['is_active']) && $filters['is_active'] == '0' ? 'selected' : '' }}>Inactive</option>
                        </select>
                    </div>
                </div>
                <div class="d-flex justify-content-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-filter me-1"></i> Apply Filters
                    </button>
                    <a href="{{ route('admin.app-versions.index') }}" class="btn btn-secondary ms-2">
                        <i class="fas fa-redo me-1"></i> Reset
                    </a>
                </div>
            </form>
        </div>
    </div>
    
    @if(isset($error))
        <div class="alert alert-danger">
            {{ $error }}
        </div>
    @else
        <!-- App Versions List -->
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">All App Versions</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Platform</th>
                                <th>Version Name</th>
                                <th>Version Code</th>
                                <th>Minimum Version</th>
                                <th>Force Update</th>
                                <th>Status</th>
                                <th>Created Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @forelse($versions as $version)
                                <tr>
                                    <td>{{ $version->id }}</td>
                                    <td>
                                        <span class="badge bg-{{ $version->platform == 'android' ? 'success' : 'primary' }}">
                                            {{ ucfirst($version->platform) }}
                                        </span>
                                    </td>
                                    <td>{{ $version->version_name }}</td>
                                    <td>{{ $version->version_code }}</td>
                                    <td>
                                        @if($version->minimum_version_name)
                                            {{ $version->minimum_version_name }} ({{ $version->minimum_version_code }})
                                        @else
                                            <span class="text-muted">Not set</span>
                                        @endif
                                    </td>
                                    <td>
                                        @if($version->force_update)
                                            <span class="badge bg-danger">Yes</span>
                                        @else
                                            <span class="badge bg-secondary">No</span>
                                        @endif
                                    </td>
                                    <td>
                                        @if($version->is_active)
                                            <span class="badge bg-success">Active</span>
                                        @else
                                            <span class="badge bg-secondary">Inactive</span>
                                        @endif
                                    </td>
                                    <td>{{ $version->created_at->format('M d, Y') }}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a href="{{ route('admin.app-versions.show', $version->id) }}" 
                                               class="btn btn-sm btn-info" 
                                               title="View Details">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                            <a href="{{ route('admin.app-versions.edit', $version->id) }}" 
                                               class="btn btn-sm btn-primary" 
                                               title="Edit">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <form action="{{ route('admin.app-versions.toggle-status', $version->id) }}" 
                                                  method="POST" 
                                                  style="display: inline;">
                                                @csrf
                                                <button type="submit" 
                                                        class="btn btn-sm btn-{{ $version->is_active ? 'warning' : 'success' }}" 
                                                        title="{{ $version->is_active ? 'Deactivate' : 'Activate' }}"
                                                        onclick="return confirm('Are you sure you want to {{ $version->is_active ? 'deactivate' : 'activate' }} this version?')">
                                                    <i class="fas fa-{{ $version->is_active ? 'pause' : 'play' }}"></i>
                                                </button>
                                            </form>
                                            <form action="{{ route('admin.app-versions.destroy', $version->id) }}" 
                                                  method="POST" 
                                                  style="display: inline;">
                                                @csrf
                                                @method('DELETE')
                                                <button type="submit" 
                                                        class="btn btn-sm btn-danger" 
                                                        title="Delete"
                                                        onclick="return confirm('Are you sure you want to delete this version?')">
                                                    <i class="fas fa-trash"></i>
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            @empty
                                <tr>
                                    <td colspan="9" class="text-center">No app versions found</td>
                                </tr>
                            @endforelse
                        </tbody>
                    </table>
                </div>
                
                <!-- Pagination -->
                <div class="d-flex justify-content-center mt-4">
                    {{ $versions->links('pagination::bootstrap-4') }}
                </div>
            </div>
        </div>
    @endif
</div>
@endsection