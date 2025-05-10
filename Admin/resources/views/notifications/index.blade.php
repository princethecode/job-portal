@extends('layouts.app')

@section('title', 'Notifications - Job Portal Admin')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h3">Notifications</h1>
        <a href="{{ route('admin.notifications.create') }}" class="btn btn-primary">
            <i class="fas fa-plus-circle"></i> Create Notification
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            @if($notifications->isEmpty())
                <div class="alert alert-info">
                    No notifications found.
                </div>
            @else
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Type</th>
                                <th>Created</th>
                                <th width="150">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($notifications as $notification)
                                <tr>
                                    <td>{{ $notification->id }}</td>
                                    <td>{{ $notification->title }}</td>
                                    <td>
                                        @if($notification->is_global)
                                            <span class="badge bg-primary">Global</span>
                                        @else
                                            <span class="badge bg-info">User Specific</span>
                                        @endif
                                    </td>
                                    <td>{{ $notification->created_at->format('M d, Y h:i A') }}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a href="{{ route('admin.notifications.show', $notification->id) }}" class="btn btn-sm btn-outline-primary">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                            <a href="{{ route('admin.notifications.edit', $notification->id) }}" class="btn btn-sm btn-outline-secondary">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <button type="button" class="btn btn-sm btn-outline-danger" 
                                                    onclick="confirmDelete('{{ $notification->id }}')">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </div>
                                        <form id="delete-form-{{ $notification->id }}" 
                                              action="{{ route('admin.notifications.destroy', $notification->id) }}" 
                                              method="POST" style="display: none;">
                                            @csrf
                                            @method('DELETE')
                                        </form>
                                    </td>
                                </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
                
                <div class="d-flex justify-content-center mt-4">
                    {{ $notifications->links() }}
                </div>
            @endif
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
    function confirmDelete(id) {
        if (confirm('Are you sure you want to delete this notification?')) {
            document.getElementById('delete-form-' + id).submit();
        }
    }
</script>
@endsection 