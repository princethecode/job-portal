@extends('layouts.app')

@section('title', 'Create Notification - Job Portal Admin')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h3">Create Notification</h1>
        <a href="{{ route('admin.notifications.index') }}" class="btn btn-secondary">
            <i class="fas fa-arrow-left"></i> Back to Notifications
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <form action="{{ route('admin.notifications.store') }}" method="POST">
                @csrf
                
                <div class="mb-3">
                    <label for="title" class="form-label">Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control @error('title') is-invalid @enderror" 
                           id="title" name="title" value="{{ old('title') }}" required>
                    @error('title')
                        <div class="invalid-feedback">{{ $message }}</div>
                    @enderror
                </div>
                
                <div class="mb-3">
                    <label for="description" class="form-label">Description <span class="text-danger">*</span></label>
                    <textarea class="form-control @error('description') is-invalid @enderror" 
                              id="description" name="description" rows="5" required>{{ old('description') }}</textarea>
                    @error('description')
                        <div class="invalid-feedback">{{ $message }}</div>
                    @enderror
                </div>
                
                <div class="mb-3 form-check">
                    <input type="checkbox" class="form-check-input" 
                           id="is_global" name="is_global" value="1" 
                           {{ old('is_global') ? 'checked' : 'checked' }}>
                    <label class="form-check-label" for="is_global">
                        Send to all users (Global Notification)
                    </label>
                </div>
                
                <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <button type="reset" class="btn btn-outline-secondary">Reset</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-bell"></i> Create & Send Notification
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection 