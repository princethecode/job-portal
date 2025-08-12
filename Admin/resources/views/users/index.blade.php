@extends('layouts.app')

@section('title', 'Users Management')

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
    .page-link span[aria-hidden="true"] {
        font-size: 14px;
        line-height: 1;
        display: inline-flex;
        align-items: center;
        justify-content: center;
    }
    .page-item:first-child .page-link span[aria-hidden="true"],
    .page-item:last-child .page-link span[aria-hidden="true"] {
        width: 14px;
        height: 14px;
    }
    .page-link:hover {
        background-color: #eaecf4;
        border-color: #dee2e6;
        color: #224abe;
    }
    .page-item.disabled .page-link {
        background-color: #f8f9fc;
        border-color: #dee2e6;
        color: #858796;
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
    /* Modal Styling */
    .modal-header {
        background-color: #f8f9fa;
        border-bottom: 1px solid #e3e6f0;
    }
    .modal-footer {
        background-color: #f8f9fa;
        border-top: 1px solid #e3e6f0;
    }
    /* Search Input Styling */
    .form-control-sm {
        height: calc(1.5em + 0.5rem + 2px);
        padding: 0.25rem 0.5rem;
        font-size: 0.875rem;
        line-height: 1.5;
        border-radius: 0.35rem;
    }
</style>
@endsection

@section('content')
<div class="container-fluid px-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="text-dark mb-0">Users Management</h2>
        <button id="bulk-delete-btn" class="btn btn-danger" disabled>
            <i class="fas fa-trash me-1"></i> Delete Selected
        </button>
    </div>
    
    @if(isset($error))
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            {{ $error }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <!-- Users List -->
    <div class="card shadow-sm">
        <div class="card-header py-3">
            <div class="d-flex justify-content-between align-items-center">
                <h6 class="m-0 font-weight-bold text-primary">All Users</h6>
                <div class="d-flex align-items-center">
                    <div class="form-check me-3">
                        <input class="form-check-input" type="checkbox" id="select-all">
                        <label class="form-check-label" for="select-all">Select All</label>
                    </div>
                    <input type="text" class="form-control form-control-sm me-2" placeholder="Search users..." id="userSearch">
                </div>
            </div>
        </div>
        <div class="card-body p-0">
            <form id="bulk-delete-form" action="{{ route('admin.users.bulk-destroy') }}" method="POST">
                @csrf
                <input type="hidden" name="_method" value="DELETE">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th width="40" class="px-4">
                                    <i class="fas fa-check-square"></i>
                                </th>
                                <th>#</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Mobile</th>
                                <th>Current Company</th>
                                <th>Expected Salary</th>
                                <th>Registered On</th>
                                <th class="text-center">Status</th>
                                <th class="text-center">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @forelse($users as $user)
                                <tr>
                                    <td class="px-4">
                                        <input class="form-check-input user-checkbox" type="checkbox" name="user_ids[]" value="{{ $user->id }}">
                                    </td>
                                    <td>{{ $user->id }}</td>
                                    <td>{{ $user->name }}</td>
                                    <td>{{ $user->email }}</td>
                                    <td>{{ $user->mobile ?? 'N/A' }}</td>
                                    <td>{{ $user->current_company ?? 'N/A' }}</td>
                                    <td>{{ isset($user->expected_salary) ? '₹'.number_format($user->expected_salary, 2) : 'N/A' }}</td>
                                    <td>{{ $user->created_at->format('M d, Y') }}</td>
                                    <td class="text-center">
                                        @if($user->is_active)
                                            <span class="badge bg-success rounded-pill">Active</span>
                                        @else
                                            <span class="badge bg-danger rounded-pill">Inactive</span>
                                        @endif
                                    </td>
                                    <td class="text-center">
                                        <div class="btn-group" role="group">
                                            <a href="{{ route('admin.users.show', $user->id) }}" class="btn btn-sm btn-info" title="View Details">
                                                <i class="fas fa-eye action-icon"></i>
                                            </a>
                                            <button type="button" class="btn btn-sm btn-{{ $user->is_active ? 'warning' : 'success' }}" 
                                                    data-bs-toggle="modal" 
                                                    data-bs-target="#statusModal{{ $user->id }}"
                                                    title="{{ $user->is_active ? 'Deactivate' : 'Activate' }} User">
                                                <i class="fas fa-{{ $user->is_active ? 'ban' : 'check' }} action-icon"></i>
                                            </button>
                                            <button type="button" class="btn btn-sm btn-danger" 
                                                    data-bs-toggle="modal" 
                                                    data-bs-target="#deleteModal{{ $user->id }}"
                                                    title="Delete User">
                                                <i class="fas fa-trash action-icon"></i>
                                            </button>
                                        </div>
                                        
                                        <!-- Status Modal -->
                                        <div class="modal fade" id="statusModal{{ $user->id }}" tabindex="-1">
                                            <div class="modal-dialog modal-dialog-centered">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">Confirm Status Change</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <p class="mb-0">Are you sure you want to {{ $user->is_active ? 'deactivate' : 'activate' }} the user "{{ $user->name }}"?</p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                        <form action="{{ route('admin.users.update-status', $user->id) }}" method="POST">
                                                            @csrf
                                                            @method('PUT')
                                                            <input type="hidden" name="is_active" value="{{ $user->is_active ? '0' : '1' }}">
                                                            <button type="submit" class="btn btn-{{ $user->is_active ? 'warning' : 'success' }}">
                                                                {{ $user->is_active ? 'Deactivate' : 'Activate' }}
                                                            </button>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <!-- Delete Modal -->
                                        <div class="modal fade" id="deleteModal{{ $user->id }}" tabindex="-1">
                                            <div class="modal-dialog modal-dialog-centered">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">Confirm Delete</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <p class="mb-0">Are you sure you want to delete the user "{{ $user->name }}"? This action cannot be undone.</p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                        <form action="{{ route('admin.users.destroy', $user->id) }}" method="POST">
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
                            @empty
                                <tr>
                                    <td colspan="10" class="text-center py-4">No users found</td>
                                </tr>
                            @endforelse
                        </tbody>
                    </table>
                </div>
                
                <!-- Bulk Delete Modal -->
                <div class="modal fade" id="bulkDeleteModal" tabindex="-1">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">Confirm Bulk Delete</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div class="modal-body">
                                <p>Are you sure you want to delete the selected users?</p>
                                <p class="text-danger mt-2 mb-0">
                                    <i class="fas fa-exclamation-triangle"></i>
                                    This will also delete all associated applications and cannot be undone.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                <button type="button" id="confirm-bulk-delete" class="btn btn-danger">Delete Selected</button>
                            </div>
                        </div>
                    </div>
                </div>
                
                @if($users->hasPages())
                    <div class="px-4 py-3 border-top">
                    {{ $users->links('pagination::bootstrap-4') }}
                    </div>
                @endif
            </form>
        </div>
    </div>
</div>
@endsection

@push('scripts')
<script>
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded');
    
    const selectAllCheckbox = document.getElementById('select-all');
    const userCheckboxes = document.querySelectorAll('.user-checkbox');
    const bulkDeleteBtn = document.getElementById('bulk-delete-btn');
    const bulkDeleteForm = document.getElementById('bulk-delete-form');
    const confirmBulkDeleteBtn = document.getElementById('confirm-bulk-delete');
    
    console.log('Select All Checkbox:', selectAllCheckbox);
    console.log('User Checkboxes:', userCheckboxes);
    console.log('Bulk Delete Button:', bulkDeleteBtn);
    console.log('Bulk Delete Form:', bulkDeleteForm);
    console.log('Confirm Bulk Delete Button:', confirmBulkDeleteBtn);
    
    if (!selectAllCheckbox || !userCheckboxes.length || !bulkDeleteBtn || !bulkDeleteForm || !confirmBulkDeleteBtn) {
        console.error('Required elements not found');
        return;
    }
    
    // Select all functionality
    selectAllCheckbox.addEventListener('change', function() {
        console.log('Select All Changed:', this.checked);
        const isChecked = this.checked;
        
        userCheckboxes.forEach(checkbox => {
            checkbox.checked = isChecked;
        });
        
        updateBulkDeleteButton();
    });
    
    // Individual checkbox change
    userCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            console.log('Individual Checkbox Changed:', this.checked);
            updateBulkDeleteButton();
            
            // Update select all checkbox
            if (!this.checked) {
                selectAllCheckbox.checked = false;
            } else {
                // Check if all checkboxes are checked
                const allChecked = Array.from(userCheckboxes).every(c => c.checked);
                selectAllCheckbox.checked = allChecked;
            }
        });
    });
    
    // Update bulk delete button state
    function updateBulkDeleteButton() {
        const checkedCount = document.querySelectorAll('.user-checkbox:checked').length;
        console.log('Checked Count:', checkedCount);
        bulkDeleteBtn.disabled = checkedCount === 0;
        bulkDeleteBtn.innerHTML = `<i class="fas fa-trash me-1"></i> Delete Selected (${checkedCount})`;
    }
    
    // Show confirmation modal when bulk delete button is clicked
    bulkDeleteBtn.addEventListener('click', function(e) {
        console.log('Bulk Delete Button Clicked');
        e.preventDefault();
        const checkedCount = document.querySelectorAll('.user-checkbox:checked').length;
        if (checkedCount > 0) {
            const bulkDeleteModal = new bootstrap.Modal(document.getElementById('bulkDeleteModal'));
            bulkDeleteModal.show();
        }
    });

    // Handle bulk delete confirmation
    confirmBulkDeleteBtn.addEventListener('click', function() {
        console.log('Confirm Bulk Delete Clicked');
        const checkedCount = document.querySelectorAll('.user-checkbox:checked').length;
        if (checkedCount > 0) {
            // Create a hidden input for the DELETE method
            const methodInput = document.createElement('input');
            methodInput.type = 'hidden';
            methodInput.name = '_method';
            methodInput.value = 'DELETE';
            bulkDeleteForm.appendChild(methodInput);
            
            // Submit the form
            bulkDeleteForm.submit();
        }
    });
    
    // Search functionality
    const searchInput = document.getElementById('userSearch');
    if (searchInput) {
        searchInput.addEventListener('keyup', function() {
            const searchText = this.value.toLowerCase();
            const rows = document.querySelectorAll('tbody tr');

            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(searchText) ? '' : 'none';
            });
        });
    }

    // Initialize button state
    updateBulkDeleteButton();
});
</script>
@endpush
