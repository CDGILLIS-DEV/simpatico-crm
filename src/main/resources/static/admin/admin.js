// Simpatico CRM Admin SPA Script

document.addEventListener('DOMContentLoaded', () => {
    // Nav Tab Switching Handler
    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', () => {
            const tabId = item.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    // Initial Load
    switchTab('dashboard');

    // Search and filter listeners
    document.getElementById('leadSearch').addEventListener('input', debounce(loadLeads, 350));
    document.getElementById('leadStatusFilter').addEventListener('change', loadLeads);
    document.getElementById('buyerSearch').addEventListener('input', debounce(loadBuyers, 350));
    document.getElementById('supplierSearch').addEventListener('input', debounce(loadSuppliers, 350));
    document.getElementById('inventorySearch').addEventListener('input', debounce(loadInventory, 350));
    document.getElementById('inventoryStatusFilter').addEventListener('change', loadInventory);
    document.getElementById('inventoryCategoryFilter').addEventListener('input', debounce(loadInventory, 350));
    document.getElementById('inventoryConditionFilter').addEventListener('input', debounce(loadInventory, 350));
});

// CSRF helpers for secure AJAX state modifications
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

function getCsrfHeaders() {
    const token = getCookie('XSRF-TOKEN');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['X-XSRF-TOKEN'] = token;
    }
    return headers;
}

// Navigation state helper
function switchTab(tabId) {
    // Toggle active classes on sidebar
    document.querySelectorAll('.menu-item').forEach(item => {
        if (item.getAttribute('data-tab') === tabId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Toggle active classes on tabs
    document.querySelectorAll('.tab-content').forEach(section => {
        if (section.id === `tab-${tabId}`) {
            section.classList.add('active');
        } else {
            section.classList.remove('active');
        }
    });

    // Trigger tab-specific loader
    if (tabId === 'dashboard') {
        loadDashboardData();
    } else if (tabId === 'leads') {
        loadLeads();
    } else if (tabId === 'buyers') {
        loadBuyers();
    } else if (tabId === 'suppliers') {
        loadSuppliers();
    } else if (tabId === 'inventory') {
        loadInventory();
    } else if (tabId === 'matches') {
        loadMatches();
    }
}

// Debounce helper for inputs
function debounce(func, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), delay);
    };
}

// Status Badges Utility
function getStatusBadge(status) {
    const formatted = (status || 'NEW').toUpperCase();
    const classMap = {
        'NEW': 'new',
        'CONTACTED': 'contacted',
        'QUALIFIED': 'qualified',
        'MATCHING': 'matching',
        'MATCHED': 'matched',
        'NEGOTIATING': 'negotiating',
        'PURCHASED': 'purchased',
        'LOST': 'lost',
        'ACTIVE': 'qualified',
        'INACTIVE': 'lost',
        'PENDING': 'contacted',
        'BLOCKED': 'lost',
        'AVAILABLE': 'qualified',
        'RESERVED': 'matching',
        'SOLD': 'purchased',
        'INITIAL': 'new',
        'REVIEWED': 'contacted',
        'PRESENTED': 'matching',
        'ACCEPTED': 'purchased',
        'REJECTED': 'lost',
        'EXPIRED': 'lost'
    };
    const cssClass = classMap[formatted] || 'new';
    return `<span class="badge badge-${cssClass}">${formatted}</span>`;
}

// ----------------------------------------------------
// Loader: Overview Dashboard
// ----------------------------------------------------
async function loadDashboardData() {
    try {
        // Load Status summaries (Fetch all matching lead statuses)
        const leadRes = await fetch('/api/leads?size=1000');
        const leadsData = await leadRes.json();
        
        // Count frequencies
        const counts = {
            NEW: 0, CONTACTED: 0, QUALIFIED: 0, MATCHING: 0,
            MATCHED: 0, PURCHASED: 0, LOST: 0
        };

        if (leadsData.content) {
            leadsData.content.forEach(lead => {
                const status = lead.status;
                if (counts[status] !== undefined) {
                    counts[status]++;
                }
            });
        }

        // Write values to cards
        Object.keys(counts).forEach(status => {
            const cardVal = document.getElementById(`count-${status}`);
            if (cardVal) {
                cardVal.innerText = counts[status];
            }
        });

        // Load 5 most recent leads
        const recentRes = await fetch('/api/leads?size=5&sort=createdAt,desc');
        const recentData = await recentRes.json();
        const recentTable = document.getElementById('recentLeadsTable');
        
        if (recentData.content && recentData.content.length > 0) {
            recentTable.innerHTML = '';
            recentData.content.forEach(lead => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${lead.buyer.firstName} ${lead.buyer.lastName}</strong></td>
                    <td>${lead.inventoryCategory}</td>
                    <td>${lead.inventoryCondition}</td>
                    <td>$${lead.budget ? lead.budget.toLocaleString() : '0'}</td>
                    <td>${new Date(lead.createdAt).toLocaleDateString()}</td>
                    <td>${getStatusBadge(lead.status)}</td>
                `;
                tr.style.cursor = 'pointer';
                tr.addEventListener('click', () => openLeadModal(lead.id));
                recentTable.appendChild(tr);
            });
        } else {
            recentTable.innerHTML = `<tr><td colspan="6" class="placeholder-row">No leads registered. Click "Public Site" below to create one.</td></tr>`;
        }
    } catch (err) {
        console.error('Failed to load Dashboard data', err);
    }
}

// ----------------------------------------------------
// Loader: Leads Management
// ----------------------------------------------------
async function loadLeads() {
    try {
        const query = document.getElementById('leadSearch').value.trim();
        const status = document.getElementById('leadStatusFilter').value;
        
        let url = `/api/leads?size=50`;
        if (status) url += `&status=${status}`;
        
        const res = await fetch(url);
        const data = await res.json();
        const tableBody = document.getElementById('leadsTableBody');
        tableBody.innerHTML = '';

        let results = data.content || [];
        
        // Search query check (name or company match locally, as backend search is status-specific)
        if (query) {
            const queryLower = query.toLowerCase();
            results = results.filter(lead => {
                const fullName = `${lead.buyer.firstName} ${lead.buyer.lastName}`.toLowerCase();
                const company = (lead.buyer.companyName || '').toLowerCase();
                return fullName.includes(queryLower) || company.includes(queryLower);
            });
        }

        if (results.length > 0) {
            results.forEach(lead => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${lead.buyer.firstName} ${lead.buyer.lastName}</strong></td>
                    <td>${lead.buyer.companyName || 'N/A'}</td>
                    <td>${lead.inventoryCategory}</td>
                    <td>${lead.inventoryCondition}</td>
                    <td>$${lead.budget ? lead.budget.toLocaleString() : '0'}</td>
                    <td>${getStatusBadge(lead.status)}</td>
                    <td><button class="btn btn-secondary btn-small" onclick="openLeadModal('${lead.id}')">Manage</button></td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = `<tr><td colspan="7" class="placeholder-row">No leads found matching criteria.</td></tr>`;
        }
    } catch (err) {
        console.error('Failed to load Leads', err);
    }
}

// ----------------------------------------------------
// Loader: Buyers Directory
// ----------------------------------------------------
async function loadBuyers() {
    try {
        const query = document.getElementById('buyerSearch').value.trim();
        const res = await fetch('/api/buyers?size=50');
        const data = await res.json();
        const tableBody = document.getElementById('buyersTableBody');
        tableBody.innerHTML = '';

        let results = data.content || [];
        if (query) {
            const q = query.toLowerCase();
            results = results.filter(b => {
                const fullName = `${b.firstName} ${b.lastName}`.toLowerCase();
                return fullName.includes(q) || b.email.toLowerCase().includes(q);
            });
        }

        if (results.length > 0) {
            results.forEach(b => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${b.firstName} ${b.lastName}</strong></td>
                    <td>${b.companyName || 'N/A'}</td>
                    <td>${b.email}</td>
                    <td>${b.phone || 'N/A'}</td>
                    <td>${b.city || ''} ${b.state || ''}</td>
                    <td>${getStatusBadge(b.active ? 'ACTIVE' : 'INACTIVE')}</td>
                    <td><button class="btn btn-secondary btn-small" onclick="openBuyerModal('${b.id}')">Profile</button></td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = `<tr><td colspan="7" class="placeholder-row">No buyers registered in database.</td></tr>`;
        }
    } catch (err) {
        console.error('Failed to load Buyers', err);
    }
}

// ----------------------------------------------------
// Loader: Suppliers Registry
// ----------------------------------------------------
async function loadSuppliers() {
    try {
        const query = document.getElementById('supplierSearch').value.trim();
        const res = await fetch('/api/suppliers?size=50');
        const data = await res.json();
        const tableBody = document.getElementById('suppliersTableBody');
        tableBody.innerHTML = '';

        let results = data.content || [];
        if (query) {
            const q = query.toLowerCase();
            results = results.filter(s => {
                const comp = s.companyName.toLowerCase();
                const contact = (s.contactName || '').toLowerCase();
                return comp.includes(q) || contact.includes(q);
            });
        }

        if (results.length > 0) {
            results.forEach(s => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${s.companyName}</strong></td>
                    <td>${s.contactName || 'N/A'}</td>
                    <td>${s.email}</td>
                    <td>${s.phone || 'N/A'}</td>
                    <td><a href="${s.website || '#'}" target="_blank">${s.website || 'N/A'}</a></td>
                    <td>${getStatusBadge(s.status)}</td>
                    <td><button class="btn btn-secondary btn-small" onclick="openSupplierModal('${s.id}')">Profile</button></td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = `<tr><td colspan="7" class="placeholder-row">No suppliers registered.</td></tr>`;
        }
    } catch (err) {
        console.error('Failed to load Suppliers', err);
    }
}

// ----------------------------------------------------
// Loader: Inventory Catalog
// ----------------------------------------------------
async function loadInventory() {
    try {
        const query = document.getElementById('inventorySearch').value.trim();
        const status = document.getElementById('inventoryStatusFilter').value;
        const category = document.getElementById('inventoryCategoryFilter').value.trim();
        const condition = document.getElementById('inventoryConditionFilter').value.trim();

        let url = `/api/inventories?size=50`;
        if (status) url += `&availability=${status}`;
        if (category) url += `&category=${category}`;
        if (condition) url += `&condition=${condition}`;

        const res = await fetch(url);
        const data = await res.json();
        const tableBody = document.getElementById('inventoryTableBody');
        tableBody.innerHTML = '';

        let results = data.content || [];
        if (query) {
            const q = query.toLowerCase();
            results = results.filter(item => {
                const title = item.title.toLowerCase();
                const desc = (item.description || '').toLowerCase();
                return title.includes(q) || desc.includes(q);
            });
        }

        if (results.length > 0) {
            results.forEach(item => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${item.title}</strong></td>
                    <td>${item.supplier ? item.supplier.companyName : 'N/A'}</td>
                    <td>${item.category}</td>
                    <td>${item.condition}</td>
                    <td>$${item.askingPrice.toLocaleString()}</td>
                    <td>${item.quantity} ${item.unitType || 'UNITS'}</td>
                    <td>${getStatusBadge(item.availabilityStatus)}</td>
                    <td><button class="btn btn-secondary btn-small" onclick="openInventoryModal('${item.id}')">Specs</button></td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = `<tr><td colspan="8" class="placeholder-row">No inventory lots available.</td></tr>`;
        }
    } catch (err) {
        console.error('Failed to load Inventory catalog', err);
    }
}

// ----------------------------------------------------
// Loader: Matched Pairs
// ----------------------------------------------------
async function loadMatches() {
    try {
        const res = await fetch('/api/matches?size=50');
        const data = await res.json();
        const tableBody = document.getElementById('matchesTableBody');
        tableBody.innerHTML = '';

        if (data.content && data.content.length > 0) {
            data.content.forEach(m => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>
                        <strong>${m.lead.buyer.firstName} ${m.lead.buyer.lastName}</strong><br>
                        <small>${m.lead.inventoryCategory} (${m.lead.inventoryCondition}) - Budget: $${m.lead.budget ? m.lead.budget.toLocaleString() : '0'}</small>
                    </td>
                    <td>
                        <strong>${m.inventory.title}</strong><br>
                        <small>Price: $${m.inventory.askingPrice.toLocaleString()} (${m.inventory.quantity} units)</small>
                    </td>
                    <td>${getStatusBadge(m.status)}</td>
                    <td>${new Date(m.createdAt).toLocaleDateString()}</td>
                    <td><button class="btn btn-secondary btn-small" onclick="openMatchModal('${m.id}')">Manage</button></td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = `<tr><td colspan="5" class="placeholder-row">No matched pairs found. Open a Lead and run the matching engine to pair offerings.</td></tr>`;
        }
    } catch (err) {
        console.error('Failed to load Matches', err);
    }
}

// ----------------------------------------------------
// Modal Controls & Actions
// ----------------------------------------------------
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

// 1. Lead Detail Modal Logic
let activeLeadId = null;
async function openLeadModal(leadId) {
    activeLeadId = leadId;
    try {
        const res = await fetch(`/api/leads/${leadId}`);
        const lead = await res.json();

        // Write Info fields
        document.getElementById('lead-buyer-name').innerText = `${lead.buyer.firstName} ${lead.buyer.lastName}`;
        document.getElementById('lead-buyer-company').innerText = lead.buyer.companyName || 'N/A';
        document.getElementById('lead-buyer-email').innerText = lead.buyer.email;
        document.getElementById('lead-buyer-phone').innerText = lead.buyer.phone || 'N/A';

        document.getElementById('lead-category').innerText = lead.inventoryCategory;
        document.getElementById('lead-condition').innerText = lead.inventoryCondition;
        document.getElementById('lead-quantity').innerText = lead.requestedQuantity || 'N/A';
        document.getElementById('lead-budget').innerText = lead.budget ? `$${lead.budget.toLocaleString()}` : 'N/A';
        document.getElementById('lead-location').innerText = lead.preferredGeographicArea || 'N/A';
        document.getElementById('lead-frequency').innerText = lead.purchaseFrequency;
        document.getElementById('leadRequirements').value = lead.additionalRequirements || 'No notes added.';

        // Configure dropdown status
        document.getElementById('leadStatusChange').value = lead.status;

        // Reset potential matches pane
        document.getElementById('potentialMatchesList').innerHTML = `<div class="placeholder-row">Click "Run Match Engine Scorer" to evaluate matching inventory lots.</div>`;

        // Render current matched list
        loadCurrentMatchesForLead(leadId);

        // Bind update status button
        document.getElementById('saveLeadStatusBtn').onclick = () => saveLeadStatus(leadId);

        // Bind Match Engine trigger
        document.getElementById('triggerMatchEngineBtn').onclick = () => runMatchEngine(leadId);

        // Open modal
        document.getElementById('leadModal').classList.add('active');
    } catch (err) {
        alert('Failed to load Lead details from server.');
    }
}

async function loadCurrentMatchesForLead(leadId) {
    try {
        const res = await fetch(`/api/leads/${leadId}/matches`);
        const matches = await res.json();
        const container = document.getElementById('potentialMatchesList');
        
        if (matches && matches.length > 0) {
            container.innerHTML = '<h4>Active matched pairs associated with this Lead:</h4>';
            matches.forEach(m => {
                const item = document.createElement('div');
                item.className = 'match-item';
                item.innerHTML = `
                    <div class="match-item-info">
                        <div class="match-item-title">${m.inventory.title}</div>
                        <div>Price: $${m.inventory.askingPrice.toLocaleString()} | Qty: ${m.inventory.quantity} units | Location: ${m.inventory.location}</div>
                        <div>Status: ${getStatusBadge(m.status)}</div>
                    </div>
                    <button class="btn btn-secondary btn-small" onclick="closeModal('leadModal'); openMatchModal('${m.id}')">Details</button>
                `;
                container.appendChild(item);
            });
        }
    } catch (err) {
        console.error('Failed to load existing matches', err);
    }
}

async function saveLeadStatus(leadId) {
    const status = document.getElementById('leadStatusChange').value;
    try {
        const res = await fetch(`/api/leads/${leadId}/status`, {
            method: 'PATCH',
            headers: getCsrfHeaders(),
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            loadLeads();
            closeModal('leadModal');
        } else {
            alert('Failed to update status. Make sure the transition is valid.');
        }
    } catch (err) {
        console.error('Failed to save status', err);
    }
}

async function runMatchEngine(leadId) {
    const listDiv = document.getElementById('potentialMatchesList');
    listDiv.innerHTML = '<div class="placeholder-row">Evaluating compatibility indices...</div>';
    
    try {
        const res = await fetch(`/api/leads/${leadId}/matches/generate`, {
            method: 'POST',
            headers: getCsrfHeaders()
        });
        const generatedMatches = await res.json();
        
        if (generatedMatches && generatedMatches.length > 0) {
            listDiv.innerHTML = '<h4>New potential matches generated and stored:</h4>';
            generatedMatches.forEach(m => {
                const item = document.createElement('div');
                item.className = 'match-item';
                item.innerHTML = `
                    <div class="match-item-info">
                        <div class="match-item-title">${m.inventory.title}</div>
                        <div>Price: $${m.inventory.askingPrice.toLocaleString()} | Qty: ${m.inventory.quantity} units | Location: ${m.inventory.location}</div>
                        <div>Status: ${getStatusBadge(m.status)}</div>
                    </div>
                    <button class="btn btn-secondary btn-small" onclick="closeModal('leadModal'); openMatchModal('${m.id}')">Manage Match</button>
                `;
                listDiv.appendChild(item);
            });
        } else {
            listDiv.innerHTML = '<div class="placeholder-row">No new compatible inventory lots found. (Category and condition must match exactly; price must be within budget).</div>';
        }
    } catch (err) {
        listDiv.innerHTML = '<div class="placeholder-row error">Execution error running Matching Engine rules.</div>';
    }
}

// 2. Buyer Profile Modal Logic
async function openBuyerModal(buyerId) {
    try {
        const res = await fetch(`/api/buyers/${buyerId}`);
        const buyer = await res.json();

        document.getElementById('buyer-name').innerText = `${buyer.firstName} ${buyer.lastName}`;
        document.getElementById('buyer-company').innerText = buyer.companyName || 'N/A';
        document.getElementById('buyer-email').innerText = buyer.email;
        document.getElementById('buyer-phone').innerText = buyer.phone || 'N/A';
        document.getElementById('buyer-status').innerText = buyer.active ? 'ACTIVE ACCOUNT' : 'INACTIVE';

        // Load buyer lead history (Query leads filtering locally/or by buyer)
        const leadsRes = await fetch(`/api/leads?size=50`);
        const leadsData = await leadsRes.json();
        const historyTable = document.getElementById('buyerLeadsHistory');
        historyTable.innerHTML = '';

        const buyerLeads = (leadsData.content || []).filter(lead => lead.buyer.id === buyerId);
        if (buyerLeads.length > 0) {
            buyerLeads.forEach(lead => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${lead.inventoryCategory}</td>
                    <td>${lead.inventoryCondition}</td>
                    <td>$${lead.budget ? lead.budget.toLocaleString() : '0'}</td>
                    <td>${getStatusBadge(lead.status)}</td>
                `;
                historyTable.appendChild(tr);
            });
        } else {
            historyTable.innerHTML = `<tr><td colspan="4" class="placeholder-row">No lead history found for this buyer.</td></tr>`;
        }

        document.getElementById('buyerModal').classList.add('active');
    } catch (err) {
        alert('Failed to load buyer profile.');
    }
}

// 3. Supplier Profile Modal Logic
async function openSupplierModal(supplierId) {
    try {
        const res = await fetch(`/api/suppliers/${supplierId}`);
        const supplier = await res.json();

        document.getElementById('supplier-company').innerText = supplier.companyName;
        document.getElementById('supplier-contact').innerText = supplier.contactName || 'N/A';
        document.getElementById('supplier-email').innerText = supplier.email;
        document.getElementById('supplier-phone').innerText = supplier.phone || 'N/A';
        document.getElementById('supplier-website').innerHTML = supplier.website ? `<a href="${supplier.website}" target="_blank">${supplier.website}</a>` : 'N/A';
        document.getElementById('supplier-status').innerText = supplier.status;

        // Query active inventory lots filtered by supplier
        const invRes = await fetch(`/api/inventories?size=100`);
        const invData = await invRes.json();
        const inventoryTable = document.getElementById('supplierInventoryList');
        inventoryTable.innerHTML = '';

        const supplierItems = (invData.content || []).filter(item => item.supplier && item.supplier.id === supplierId);
        if (supplierItems.length > 0) {
            supplierItems.forEach(item => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${item.title}</strong></td>
                    <td>${item.category}</td>
                    <td>${item.condition}</td>
                    <td>$${item.askingPrice.toLocaleString()}</td>
                    <td>${getStatusBadge(item.availabilityStatus)}</td>
                `;
                inventoryTable.appendChild(tr);
            });
        } else {
            inventoryTable.innerHTML = `<tr><td colspan="5" class="placeholder-row">No active inventory packages registered.</td></tr>`;
        }

        document.getElementById('supplierModal').classList.add('active');
    } catch (err) {
        alert('Failed to load supplier details.');
    }
}

// 4. Inventory Lot Details Modal Logic
async function openInventoryModal(inventoryId) {
    try {
        const res = await fetch(`/api/inventories/${inventoryId}`);
        const item = await res.json();

        document.getElementById('inv-title').innerText = item.title;
        document.getElementById('inv-category').innerText = item.category;
        document.getElementById('inv-condition').innerText = item.condition;
        document.getElementById('inv-quantity').innerText = `${item.quantity} ${item.unitType || 'UNITS'}`;
        document.getElementById('inv-price').innerText = `$${item.askingPrice.toLocaleString()}`;
        document.getElementById('inv-location').innerText = item.location || 'N/A';
        document.getElementById('inv-status').innerText = item.availabilityStatus;

        document.getElementById('inv-supplier-company').innerText = item.supplier ? item.supplier.companyName : 'N/A';
        document.getElementById('inv-supplier-contact').innerText = item.supplier ? (item.supplier.contactName || 'N/A') : 'N/A';
        document.getElementById('inv-supplier-email').innerText = item.supplier ? item.supplier.email : 'N/A';
        document.getElementById('invDescription').value = item.description || 'No description provided.';

        document.getElementById('inventoryModal').classList.add('active');
    } catch (err) {
        alert('Failed to load inventory details.');
    }
}

// 5. Match Pair details & updates Modal Logic
async function openMatchModal(matchId) {
    try {
        const res = await fetch(`/api/matches/${matchId}`);
        const match = await res.json();

        document.getElementById('match-buyer-desc').innerHTML = `
            <strong>${match.lead.buyer.firstName} ${match.lead.buyer.lastName}</strong> (Company: ${match.lead.buyer.companyName || 'N/A'})<br>
            Category: ${match.lead.inventoryCategory} | Condition: ${match.lead.inventoryCondition} | Budget: $${match.lead.budget ? match.lead.budget.toLocaleString() : '0'}
        `;

        document.getElementById('match-inv-desc').innerHTML = `
            <strong>${match.inventory.title}</strong> (Supplier: ${match.inventory.supplier ? match.inventory.supplier.companyName : 'N/A'})<br>
            Price: $${match.inventory.askingPrice.toLocaleString()} | Qty: ${match.inventory.quantity} units | Location: ${match.inventory.location || 'N/A'}
        `;

        document.getElementById('match-current-status').innerText = match.status;
        document.getElementById('matchStatusSelect').value = match.status;
        document.getElementById('matchNotesInput').value = match.notes || '';

        // Bind update button
        document.getElementById('saveMatchStatusBtn').onclick = () => saveMatchStatus(matchId);

        document.getElementById('matchModal').classList.add('active');
    } catch (err) {
        alert('Failed to load match record details.');
    }
}

async function saveMatchStatus(matchId) {
    const status = document.getElementById('matchStatusSelect').value;
    const notes = document.getElementById('matchNotesInput').value.trim();

    try {
        const res = await fetch(`/api/matches/${matchId}/status`, {
            method: 'PATCH',
            headers: getCsrfHeaders(),
            body: JSON.stringify({ status, notes })
        });
        if (res.ok) {
            loadMatches();
            closeModal('matchModal');
        } else {
            alert('Failed to update match status.');
        }
    } catch (err) {
        console.error('Failed to save match settings', err);
    }
}
