@extends('layouts.app')

@section('title', 'Notifications - Job Portal Admin')

@section('styles')
<style>
    .table th {
        background-color: #f8f9fa;
        font-weight: 600;
    }
    .badge {
        padding: 0.5em 0.8em;
        font-size: 0.8em;
    }
    .btn-group .btn {
        margin: 0 2px;
        padding: 0.25rem 0.5rem;
        font-size: 0.875rem;
    }
    .btn-group .btn i {
        margin: 0;
        font-size: 0.875rem;
        width: 14px;
        height: 14px;
        line-height: 14px;
    }
    .action-icon {
        font-size: 14px;
        width: 14px;
        height: 14px;
        line-height: 14px;
        display: inline-block;
    }
    /* Updated Pagination Styling */
    .pagination {
        margin: 0;
        justify-content: center;
    }
    .page-item:not(.active) .page-link {
        background-color: #fff;
        border-color: #dee2e6;
        color: #4e73df;
    }
    .page-item.active .page-link {
        background-color: #4e73df;
        border-color: #4e73df;
    }
    .page-link {
        padding: 0.5rem 0.75rem;
        margin: 0 3px;
        border-radius: 0.35rem;
        line-height: 1.25;
        font-size: 0.875rem;
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 32px;
    }
    /* Card and Table Styling */
    .card {
        box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15) !important;
        border: none;
    }
    .card-header {
        background-color: #f8f9fa;
        border-bottom: 1px solid #e3e6f0;
        padding: 1rem 1.25rem;
    }
    .table-responsive {
        padding: 0;
    }
    .table {
        margin-bottom: 0;
    }
    .table td {
        vertical-align: middle;
        padding: 0.75rem 1rem;
    }
    .table th {
        padding: 0.75rem 1rem;
        font-weight: 600;
        text-transform: uppercase;
        font-size: 0.8rem;
        letter-spacing: 0.03em;
        border-bottom: 2px solid #e3e6f0;
    }
</style>
@endsection

@section('content')
<div class="container-fluid px-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="text-dark mb-0">Notifications Management</h2>
        <a href="{{ route('admin.notifications.create') }}" class="btn btn-primary">
            <i class="fas fa-plus-circle me-1"></i> Create Notification
        </a>
    </div>

    @if(session('success'))
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        {{ session('success') }}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    @endif

    <div class="card shadow-sm">
        <div class="card-header py-3">
            <div class="d-flex justify-content-between align-items-center">
                <h6 class="m-0 font-weight-bold text-primary">All Notifications</h6>
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control form-control-sm me-2" placeholder="Search notifications..." id="notificationSearch">
                </div>
            </div>
        </div>
        <div class="card-body p-0">
            @if($notifications->isEmpty())
                <div class="alert alert-info m-3">
                    No notifications found.
                </div>
            @else
                <div class="table-responsive">
                    <table class="table table-hover mb-0" id="notificationsTable">
                        <thead>
                            <tr>
                                <th class="px-4">ID</th>
                                <th>Title</th>
                                <th>Description</th>
                                <th class="text-center">Type</th>
                                <th>Created At</th>
                                <th class="text-center">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($notifications as $notification)
                                <tr>
                                    <td class="px-4">{{ $notification->id }}</td>
                                    <td>{{ $notification->title }}</td>
                                    <td>{{ \Illuminate\Support\Str::limit($notification->description, 50) }}</td>
                                    <td class="text-center">
                                        @if($notification->is_global)
                                            <span class="badge bg-primary rounded-pill">Global</span>
                                        @else
                                            <span class="badge bg-info rounded-pill">User Specific</span>
                                        @endif
                                    </td>
                                    <td>{{ $notification->created_at->format('M d, Y h:i A') }}</td>
                                    <td class="text-center">
                                        <div class="btn-group" role="group">
                                            <a href="{{ route('admin.notifications.show', $notification->id) }}" class="btn btn-sm btn-info" title="View Details">
                                                <i class="fas fa-eye action-icon"></i>
                                            </a>
                                            <a href="{{ route('admin.notifications.edit', $notification->id) }}" class="btn btn-sm btn-warning" title="Edit Notification">
                                                <i class="fas fa-edit action-icon"></i>
                                            </a>
                                            <button type="button" class="btn btn-sm btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal{{ $notification->id }}" title="Delete Notification">
                                                <i class="fas fa-trash action-icon"></i>
                                            </button>
                                        </div>
                                        
                                        <!-- Delete Modal -->
                                        <div class="modal fade" id="deleteModal{{ $notification->id }}" tabindex="-1">
                                            <div class="modal-dialog modal-dialog-centered">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">Confirm Delete</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <p class="mb-0">Are you sure you want to delete the notification "{{ $notification->title }}"? This action cannot be undone.</p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                        <form action="{{ route('admin.notifications.destroy', $notification->id) }}" method="POST">
                                                            @csrf
                                                            @method('DELETE')
                                                            <button type="submit" class="btn btn-danger">Delete</button>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
                
                <div class="d-flex justify-content-center py-3">
                    {{ $notifications->links() }}
                </div>
            @endif
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Simple search functionality
        const searchInput = document.getElementById('notificationSearch');
        if (searchInput) {
            searchInput.addEventListener('keyup', function() {
                const searchText = this.value.toLowerCase();
                const table = document.getElementById('notificationsTable');
                const rows = table.getElementsByTagName('tr');
                
                for (let i = 1; i < rows.length; i++) {
                    const row = rows[i];
                    const cells = row.getElementsByTagName('td');
                    let found = false;
                    
                    for (let j = 0; j < cells.length - 1; j++) {
                        if (cells[j].textContent.toLowerCase().includes(searchText)) {
                            found = true;
                            break;
                        }
                    }
                    
                    row.style.display = found ? '' : 'none';
                }
            });
        }
    });
</script>
@endsection 