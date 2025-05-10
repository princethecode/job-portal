@extends('layouts.app')

@section('title', 'View Notification - Job Portal Admin')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h3">View Notification</h1>
        <div>
            <a href="{{ route('admin.notifications.edit', $notification->id) }}" class="btn btn-primary">
                <i class="fas fa-edit"></i> Edit
            </a>
            <a href="{{ route('admin.notifications.index') }}" class="btn btn-secondary">
                <i class="fas fa-arrow-left"></i> Back to Notifications
            </a>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-header">
            <h5 class="card-title mb-0">Notification Details</h5>
        </div>
        <div class="card-body">
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">ID:</div>
                <div class="col-md-9">{{ $notification->id }}</div>
            </div>
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">Title:</div>
                <div class="col-md-9">{{ $notification->title }}</div>
            </div>
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">Description:</div>
                <div class="col-md-9">{{ $notification->description }}</div>
            </div>
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">Type:</div>
                <div class="col-md-9">
                    @if($notification->is_global)
                        <span class="badge bg-primary">Global</span>
                    @else
                        <span class="badge bg-info">User Specific</span>
                    @endif
                </div>
            </div>
            @if(!$notification->is_global && $notification->user_id)
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">User:</div>
                <div class="col-md-9">
                    <a href="{{ route('admin.users.show', $notification->user_id) }}">
                        {{ $notification->user->name ?? 'Unknown User' }}
                    </a>
                </div>
            </div>
            @endif
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">Created:</div>
                <div class="col-md-9">{{ $notification->created_at->format('M d, Y h:i A') }}</div>
            </div>
            <div class="row mb-3">
                <div class="col-md-3 fw-bold">Updated:</div>
                <div class="col-md-9">{{ $notification->updated_at->format('M d, Y h:i A') }}</div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-header">
            <h5 class="card-title mb-0">Actions</h5>
        </div>
        <div class="card-body d-flex justify-content-between">
            <a href="{{ route('admin.notifications.edit', $notification->id) }}" class="btn btn-primary">
                <i class="fas fa-edit"></i> Edit Notification
            </a>
            <button type="button" class="btn btn-danger" onclick="confirmDelete()">
                <i class="fas fa-trash"></i> Delete Notification
            </button>
            <form id="delete-form" action="{{ route('admin.notifications.destroy', $notification->id) }}" method="POST" style="display: none;">
                @csrf
                @method('DELETE')
            </form>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
    function confirmDelete() {
        if (confirm('Are you sure you want to delete this notification?')) {
            document.getElementById('delete-form').submit();
        }
    }
</script>
@endsection 