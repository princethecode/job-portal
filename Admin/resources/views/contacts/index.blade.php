@extends('layouts.app')

@section('content')
<div class="container-fluid">
    <div class="row mb-3">
        <div class="col-12 d-flex justify-content-end">
            <div class="dropdown me-2">
                <button class="btn btn-primary dropdown-toggle" type="button" id="exportDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="fas fa-file-excel"></i> Export to Excel
                </button>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="exportDropdown">
                    <li>
                        <a class="dropdown-item" href="{{ route('contacts.export') }}">
                            <i class="fas fa-file-export"></i> Export All Contacts
                        </a>
                    </li>
                    @if($selectedLabel)
                        <li>
                            <a class="dropdown-item" href="{{ route('contacts.export', ['label_id' => $selectedLabel->id]) }}">
                                <i class="fas fa-tag"></i> Export {{ $selectedLabel->name }} Contacts
                            </a>
                        </li>
                    @endif
                    @if(request('search'))
                        <li>
                            <a class="dropdown-item" href="{{ route('contacts.export', ['search' => request('search')]) }}">
                                <i class="fas fa-search"></i> Export Search Results
                            </a>
                        </li>
                    @endif
                </ul>
            </div>
            <a href="{{ route('contacts.refresh-database') }}" class="btn btn-warning" onclick="return confirm('Are you sure you want to refresh the database? This will remove all duplicate phone numbers, keeping only the oldest record for each number.')">
                <i class="fas fa-sync-alt"></i> Refresh Database
            </a>
        </div>
    </div>
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Contact Management</h3>
                    @if($selectedLabel)
                        <div class="mt-2">
                            <span class="badge" style="background-color: {{ $selectedLabel->color }}">
                                {{ $selectedLabel->name }}
                            </span>
                            <a href="{{ route('contacts.index') }}" class="btn btn-sm btn-secondary ml-2">
                                Clear Filter
                            </a>
                        </div>
                    @endif
                </div>
                <div class="card-body">
                    <!-- Upload Form -->
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header">
                                    <h4>Upload Contacts</h4>
                                </div>
                                <div class="card-body">
                                    <form action="{{ route('contacts.upload') }}" method="POST" enctype="multipart/form-data">
                                        @csrf
                                        <div class="form-group">
                                            <label for="csv_file">Select CSV File</label>
                                            <input type="file" class="form-control-file" id="csv_file" name="csv_file" accept=".csv" required>
                                            <small class="form-text text-muted">File should contain columns: Name, PhoneNumber, Email</small>
                                        </div>
                                        <button type="submit" class="btn btn-primary">Upload & Process</button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <!-- Labels Section -->
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header">
                                    <h4>Labels</h4>
                                </div>
                                <div class="card-body">
                                    <!-- Create Label Form -->
                                    <form action="{{ route('contacts.store-label') }}" method="POST" class="mb-4">
                                        @csrf
                                        <div class="form-group">
                                            <label for="label_name">Create New Label</label>
                                            <div class="input-group">
                                                <input type="text" 
                                                       class="form-control" 
                                                       id="label_name" 
                                                       name="name" 
                                                       required 
                                                       placeholder="Label name">
                                                <input type="color" 
                                                       class="form-control" 
                                                       id="label_color" 
                                                       name="color" 
                                                       value="#000000" 
                                                       required 
                                                       style="max-width: 100px;">
                                                <div class="input-group-append">
                                                    <button type="submit" class="btn btn-primary">Create</button>
                                                </div>
                                            </div>
                                        </div>
                                    </form>

                                    <!-- Labels List -->
                                    <div class="labels-list">
                                        <div class="d-flex justify-content-between align-items-center mb-3">
                                            <h5 class="mb-0">Available Labels</h5>
                                            <div class="btn-group">
                                                <form action="{{ route('contacts.sync-labels') }}" method="POST" class="d-inline">
                                                    @csrf
                                                    <button type="submit" class="btn btn-info btn-sm">
                                                        <i class="fas fa-sync"></i> Sync Labels
                                                    </button>
                                                </form>
                                                <a href="{{ route('labels.index') }}" class="btn btn-primary btn-sm">
                                                    <i class="fas fa-edit"></i> Edit Labels
                                                </a>
                                            </div>
                                        </div>
                                        <div class="d-flex flex-wrap gap-2">
                                            @foreach($labels as $label)
                                                <a href="{{ route('contacts.index', array_merge(request()->except('page'), ['label_id' => $label->id])) }}" 
                                                   class="badge {{ $selectedLabel && $selectedLabel->id === $label->id ? 'border border-dark' : '' }}"
                                                   style="background-color: {{ $label->color }}; cursor: pointer; text-decoration: none;">
                                                    {{ $label->name }}
                                                    <small class="ml-1">({{ $label->contacts_count }})</small>
                                                </a>
                                            @endforeach
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Search Form -->
                    <div class="row mb-4">
                        <div class="col-12">
                            <div class="card">
                                <div class="card-header">
                                    <h4>Search Contacts</h4>
                                </div>
                                <div class="card-body">
                                    <form action="{{ route('contacts.index') }}" method="GET" class="mb-0">
                                        @if(request('label_id'))
                                            <input type="hidden" name="label_id" value="{{ request('label_id') }}">
                                        @endif
                                        <div class="input-group">
                                            <input type="text" 
                                                   class="form-control" 
                                                   name="search" 
                                                   placeholder="Search by name, phone, or email..."
                                                   value="{{ request('search') }}">
                                            <div class="input-group-append">
                                                <button type="submit" class="btn btn-primary">
                                                    <i class="fas fa-search"></i> Search
                                                </button>
                                                @if(request('search'))
                                                    <a href="{{ route('contacts.index', request()->except('search')) }}" 
                                                       class="btn btn-secondary">
                                                        <i class="fas fa-times"></i> Clear
                                                    </a>
                                                @endif
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Per Page Selector -->
                    <div class="row mb-3">
                        <div class="col-12">
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center">
                                    <label for="perPage" class="me-2">Show</label>
                                    <select id="perPage" class="form-select" style="width: auto;">
                                        <option value="25" {{ request('per_page', 25) == 25 ? 'selected' : '' }}>25</option>
                                        <option value="50" {{ request('per_page', 25) == 50 ? 'selected' : '' }}>50</option>
                                        <option value="75" {{ request('per_page', 25) == 75 ? 'selected' : '' }}>75</option>
                                        <option value="100" {{ request('per_page', 25) == 100 ? 'selected' : '' }}>100</option>
                                        <option value="250" {{ request('per_page', 25) == 250 ? 'selected' : '' }}>250</option>
                                    </select>
                                    <span class="ms-2">entries</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Contacts Table -->
                    @if(isset($contacts) && count($contacts) > 0)
                    <div class="table-responsive">
                        <div class="mb-3">
                            <strong>Showing {{ $contacts->firstItem() }} to {{ $contacts->lastItem() }} of {{ $contacts->total() }} contacts</strong>
                            @if(request('search'))
                                <span class="text-muted">for "{{ request('search') }}"</span>
                            @endif
                            <div class="float-end">
                                <span id="selectedCount" class="me-2" style="display: none;">
                                    <span class="badge bg-primary">0</span> contacts selected
                                </span>
                                <button type="button" class="btn btn-danger" id="bulkDeleteBtn" style="display: none;">
                                    <i class="fas fa-trash"></i> Delete Selected
                                </button>
                            </div>
                        </div>
                        <form id="bulkDeleteForm" action="{{ route('contacts.bulk-delete') }}" method="POST">
                            @csrf
                            <table class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>
                                            <input type="checkbox" id="selectAll" class="form-check-input">
                                        </th>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Phone Number</th>
                                        <th>Country Code</th>
                                        <th>Email</th>
                                        <th>Label</th>
                                        <th>Import Tag</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @foreach($contacts as $contact)
                                    <tr>
                                        <td>
                                            <input type="checkbox" name="contact_ids[]" value="{{ $contact->id }}" class="form-check-input contact-checkbox">
                                        </td>
                                        <td>{{ $contact->id }}</td>
                                        <td>{{ $contact->name }}</td>
                                        <td>{{ $contact->phone_number }}</td>
                                        <td>{{ $contact->country_code }}</td>
                                        <td>{{ $contact->email }}</td>
                                        <td>
                                            <div class="d-flex flex-wrap gap-1">
                                                @foreach($contact->labels as $label)
                                                    <span class="badge" style="background-color: {{ $label->color }};">
                                                        {{ $label->name }}
                                                        <form action="{{ route('contacts.update-label', $contact) }}" method="POST" class="d-inline">
                                                            @csrf
                                                            <input type="hidden" name="label_ids[]" value="{{ $label->id }}">
                                                            <input type="hidden" name="action" value="remove">
                                                            <button type="submit" class="btn btn-link btn-sm p-0 ml-1 text-white" 
                                                                    onclick="return confirm('Remove this label?')" 
                                                                    style="text-decoration: none;">
                                                                <i class="fas fa-times"></i>
                                                            </button>
                                                        </form>
                                                    </span>
                                                @endforeach
                                            </div>
                                        </td>
                                        <td>{{ $contact->import_tag }}</td>
                                        <td>
                                            <div class="btn-group">
                                                <button type="button" 
                                                        class="btn btn-sm btn-primary" 
                                                        data-bs-toggle="modal" 
                                                        data-bs-target="#labelModal{{ $contact->id }}"
                                                        title="Edit Labels">
                                                    <i class="fas fa-edit"></i>
                                                </button>
                                                <button type="button" 
                                                        class="btn btn-sm btn-danger" 
                                                        onclick="if(confirm('Are you sure you want to remove all labels from this contact?')) {
                                                            document.getElementById('removeAllLabels{{ $contact->id }}').submit();
                                                        }"
                                                        title="Remove All Labels">
                                                    <i class="fas fa-trash"></i>
                                                </button>
                                            </div>

                                            <!-- Hidden form for removing all labels -->
                                            <form id="removeAllLabels{{ $contact->id }}" 
                                                  action="{{ route('contacts.update-label', $contact) }}" 
                                                  method="POST" 
                                                  style="display: none;">
                                                @csrf
                                                <input type="hidden" name="label_ids" value="[]">
                                            </form>

                                            <!-- Label Modal -->
                                            <div class="modal fade" id="labelModal{{ $contact->id }}" tabindex="-1" aria-labelledby="labelModalLabel{{ $contact->id }}" aria-hidden="true">
                                                <div class="modal-dialog">
                                                    <div class="modal-content">
                                                        <form action="{{ route('contacts.update-label', $contact) }}" method="POST">
                                                            @csrf
                                                            <div class="modal-header">
                                                                <h5 class="modal-title" id="labelModalLabel{{ $contact->id }}">Manage Labels for {{ $contact->name }}</h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <div class="modal-body">
                                                                <div class="form-group">
                                                                    <label>Select Labels</label>
                                                                    <div class="d-flex flex-wrap gap-2">
                                                                        @foreach($labels as $label)
                                                                            <div class="custom-control custom-checkbox">
                                                                                <input type="checkbox" 
                                                                                       class="custom-control-input" 
                                                                                       id="label{{ $contact->id }}_{{ $label->id }}" 
                                                                                       name="label_ids[]" 
                                                                                       value="{{ $label->id }}"
                                                                                       {{ $contact->hasLabel($label->id) ? 'checked' : '' }}>
                                                                                <label class="custom-control-label" 
                                                                                       for="label{{ $contact->id }}_{{ $label->id }}"
                                                                                       style="color: {{ $label->color }}">
                                                                                    {{ $label->name }}
                                                                                </label>
                                                                            </div>
                                                                        @endforeach
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                                                                <button type="submit" class="btn btn-primary">Save Changes</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                    @endforeach
                                </tbody>
                            </table>

                            <!-- Pagination -->
                            <div class="d-flex justify-content-center mt-4">
                                {{ $contacts->links('pagination::bootstrap-4') }}
                            </div>
                        </div>
                    </div>
                    @else
                        <div class="alert alert-info">
                            @if(request('search'))
                                No contacts found matching "{{ request('search') }}"
                            @else
                                No contacts found
                            @endif
                        </div>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@push('styles')
<style>
    .table th {
        background-color: #f8f9fa;
    }
    .badge {
        padding: 0.5em 0.75em;
        color: white;
    }
    .labels-list {
        margin-top: 1rem;
    }
    .gap-2 {
        gap: 0.5rem;
    }
    .badge:hover {
        opacity: 0.9;
    }
    .input-group-append .btn {
        margin-left: -1px;
    }
    .custom-control-label {
        padding-left: 0.5rem;
    }
    .custom-control {
        margin-right: 1rem;
    }
    .modal-body .gap-2 {
        gap: 0.5rem;
    }
</style>
@endpush

@push('scripts')
<script>
document.addEventListener('DOMContentLoaded', function() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const contactCheckboxes = document.querySelectorAll('.contact-checkbox');
    const bulkDeleteBtn = document.getElementById('bulkDeleteBtn');
    const bulkDeleteForm = document.getElementById('bulkDeleteForm');
    const selectedCount = document.getElementById('selectedCount');
    const countBadge = selectedCount.querySelector('.badge');
    const perPageSelect = document.getElementById('perPage');

    // Handle per page change
    perPageSelect.addEventListener('change', function() {
        const currentUrl = new URL(window.location.href);
        currentUrl.searchParams.set('per_page', this.value);
        window.location.href = currentUrl.toString();
    });

    // Handle select all checkbox
    selectAllCheckbox.addEventListener('change', function() {
        contactCheckboxes.forEach(checkbox => {
            checkbox.checked = this.checked;
        });
        updateBulkDeleteButton();
    });

    // Handle individual checkboxes
    contactCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateBulkDeleteButton();
            // Update select all checkbox state
            selectAllCheckbox.checked = Array.from(contactCheckboxes).every(cb => cb.checked);
        });
    });

    // Update bulk delete button visibility and count
    function updateBulkDeleteButton() {
        const checkedCount = document.querySelectorAll('.contact-checkbox:checked').length;
        bulkDeleteBtn.style.display = checkedCount > 0 ? 'inline-block' : 'none';
        selectedCount.style.display = checkedCount > 0 ? 'inline-block' : 'none';
        countBadge.textContent = checkedCount;
    }

    // Handle bulk delete button click
    bulkDeleteBtn.addEventListener('click', function() {
        const checkedCount = document.querySelectorAll('.contact-checkbox:checked').length;
        if (confirm(`Are you sure you want to delete ${checkedCount} selected contact(s)?`)) {
            bulkDeleteForm.submit();
        }
    });
});
</script>
@endpush 