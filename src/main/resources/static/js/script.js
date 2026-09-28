// ================================================================
// CONTRACT WATCH - FRONTEND SCRIPT
// ================================================================

const $ = (id) => document.getElementById(id);

function showMessage(element, message, isError = false) {
    if (!element) return;
    element.textContent = message;
    element.classList.toggle("error", isError);
}

async function readError(response) {
    try {
        const data = await response.json();
        if (data.fields) {
            return Object.values(data.fields).join(" | ");
        }
        return data.error || data.message || "Request failed.";
    } catch (e) {
        return "Request failed.";
    }
}

// ---------------------------------------------------------------
// ADD VENDOR
// ---------------------------------------------------------------

const vendorForm = $("vendorForm");
if (vendorForm) {
    vendorForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = $("message");

        const vendor = {
            name: $("name").value.trim(),
            email: $("email").value.trim() || null,
            phone: $("phone").value.trim() || null
        };

        if (vendor.name.length < 2) {
            showMessage(message, "Vendor name must contain at least 2 characters.", true);
            return;
        }

        try {
            const response = await fetch("/vendors", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(vendor)
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            vendorForm.reset();
            showMessage(message, "Vendor created successfully.");
        } catch (error) {
            showMessage(message, error.message, true);
        }
    });
}

// ---------------------------------------------------------------
// ADD CONTRACT
// ---------------------------------------------------------------

const contractForm = $("contractForm");
if (contractForm) {
    loadVendorsIntoSelect();

    contractForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = $("message");

        const request = {
            contractName: $("contractName").value.trim(),
            vendorId: Number($("vendorId").value),
            startDate: $("startDate").value,
            endDate: $("endDate").value,
            renewalNoticePeriodDays: Number($("noticeDays").value)
        };

        if (!request.vendorId) {
            showMessage(message, "Please select a vendor.", true);
            return;
        }

        if (request.endDate <= request.startDate) {
            showMessage(message, "End date must be after start date.", true);
            return;
        }

        if (request.renewalNoticePeriodDays < 0) {
            showMessage(message, "Notice period cannot be negative.", true);
            return;
        }

        try {
            const response = await fetch("/contracts", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(request)
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            const contract = await response.json();
            contractForm.reset();
            $("noticeDays").value = 30;
            showMessage(message,
                contract.renewalReviewFlag
                    ? "Contract created and automatically flagged for renewal review."
                    : "Contract created successfully."
            );
        } catch (error) {
            showMessage(message, error.message, true);
        }
    });
}

async function loadVendorsIntoSelect() {
    const select = $("vendorId");
    if (!select) return;

    try {
        const response = await fetch("/vendors");
        if (!response.ok) throw new Error(await readError(response));
        const vendors = await response.json();

        select.innerHTML = '<option value="">Select vendor</option>';
        vendors.forEach(vendor => {
            const option = document.createElement("option");
            option.value = vendor.id;
            option.textContent = `${vendor.name} (ID: ${vendor.id})`;
            select.appendChild(option);
        });
    } catch (error) {
        select.innerHTML = '<option value="">Could not load vendors</option>';
    }
}

// ---------------------------------------------------------------
// ALL CONTRACTS
// ---------------------------------------------------------------

const contractTableBody = $("contractTableBody");
if (contractTableBody) {
    loadContracts();
}

async function loadContracts() {
    try {
        const response = await fetch("/contracts");
        if (!response.ok) throw new Error(await readError(response));
        const contracts = await response.json();
        contractTableBody.innerHTML = "";

        if (contracts.length === 0) {
            contractTableBody.innerHTML = '<tr><td colspan="9">No contracts found.</td></tr>';
            return;
        }

        contracts.forEach(contract => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${contract.id}</td>
                <td>${escapeHtml(contract.contractName)}</td>
                <td>${escapeHtml(contract.vendor?.name || "")}</td>
                <td>${contract.startDate || ""}</td>
                <td>${contract.endDate || ""}</td>
                <td>${contract.renewalNoticePeriodDays}</td>
                <td>${contract.status}</td>
                <td>${contract.renewalReviewFlag ? "YES" : "NO"}</td>
                <td class="actions-cell">
                    <button class="button" onclick="openDecision(${contract.id})">Decision</button>
                    <button class="button" onclick="openDocuments(${contract.id})">Documents</button>
                    <button class="button danger-button" onclick="deleteContract(${contract.id})">Delete</button>
                </td>
            `;
            contractTableBody.appendChild(row);
        });
    } catch (error) {
        contractTableBody.innerHTML = `<tr><td colspan="9">${escapeHtml(error.message)}</td></tr>`;
    }
}

function openDecision(id) {
    window.location.href = `/renewal-decision.html?id=${id}`;
}

function openDocuments(id) {
    window.location.href = `/documents.html?id=${id}`;
}

async function deleteContract(id) {
    if (!confirm("Are you sure you want to delete this contract?")) return;

    try {
        const response = await fetch(`/contracts/${id}`, { method: "DELETE" });
        if (!response.ok) throw new Error(await readError(response));
        location.reload();
    } catch (error) {
        alert(error.message);
    }
}

// ---------------------------------------------------------------
// RENEWAL REMINDERS
// ---------------------------------------------------------------

const reminderTableBody = $("reminderTableBody");
if (reminderTableBody) {
    loadReminders();
}

async function loadReminders() {
    try {
        const response = await fetch("/contracts/renewal-reminders");
        if (!response.ok) throw new Error(await readError(response));
        const contracts = await response.json();
        reminderTableBody.innerHTML = "";

        if (contracts.length === 0) {
            reminderTableBody.innerHTML = '<tr><td colspan="6">No active renewal reminders right now.</td></tr>';
            return;
        }

        contracts.forEach(contract => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${contract.id}</td>
                <td>${escapeHtml(contract.contractName)}</td>
                <td>${escapeHtml(contract.vendor?.name || "")}</td>
                <td>${contract.endDate}</td>
                <td>${contract.renewalNoticePeriodDays}</td>
                <td><button class="button" onclick="openDecision(${contract.id})">Record Decision</button></td>
            `;
            reminderTableBody.appendChild(row);
        });
    } catch (error) {
        reminderTableBody.innerHTML = `<tr><td colspan="6">${escapeHtml(error.message)}</td></tr>`;
    }
}

// ---------------------------------------------------------------
// EXPIRING IN 30 DAYS
// ---------------------------------------------------------------

const expiringTableBody = $("expiringTableBody");
if (expiringTableBody) {
    loadExpiringContracts();
}

async function loadExpiringContracts() {
    try {
        const response = await fetch("/contracts/expiring?days=30");
        if (!response.ok) throw new Error(await readError(response));
        const contracts = await response.json();
        expiringTableBody.innerHTML = "";

        if (contracts.length === 0) {
            expiringTableBody.innerHTML = '<tr><td colspan="6">No active contracts are expiring in the next 30 days.</td></tr>';
            return;
        }

        contracts.forEach(contract => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${contract.id}</td>
                <td>${escapeHtml(contract.contractName)}</td>
                <td>${escapeHtml(contract.vendor?.name || "")}</td>
                <td>${contract.endDate}</td>
                <td>${contract.status}</td>
                <td><button class="button" onclick="openDecision(${contract.id})">Decision</button></td>
            `;
            expiringTableBody.appendChild(row);
        });
    } catch (error) {
        expiringTableBody.innerHTML = `<tr><td colspan="6">${escapeHtml(error.message)}</td></tr>`;
    }
}

// ---------------------------------------------------------------
// RENEWAL DECISION
// ---------------------------------------------------------------

const decisionForm = $("decisionForm");
if (decisionForm) {
    const params = new URLSearchParams(window.location.search);
    const contractId = params.get("id");
    const decisionDate = $("decisionDate");
    decisionDate.value = new Date().toISOString().slice(0, 10);

    if (!contractId) {
        showMessage($("message"), "Contract ID is missing.", true);
    } else {
        loadDecisionContract(contractId);

        decisionForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            const message = $("message");
            const decision = $("decision").value;
            const newEndDate = $("newEndDate").value || null;

            if (decision === "RENEWED" && !newEndDate) {
                showMessage(message, "New end date is required when the contract is renewed.", true);
                return;
            }

            const request = {
                decision,
                decisionDate: decisionDate.value || null,
                newEndDate,
                notes: $("notes").value.trim() || null
            };

            try {
                const response = await fetch(`/contracts/${contractId}/renewal-decisions`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(request)
                });

                if (!response.ok) throw new Error(await readError(response));

                showMessage(message, "Renewal decision saved successfully.");
                decisionForm.reset();
                decisionDate.value = new Date().toISOString().slice(0, 10);
            } catch (error) {
                showMessage(message, error.message, true);
            }
        });
    }
}

async function loadDecisionContract(id) {
    try {
        const response = await fetch(`/contracts/${id}`);
        if (!response.ok) throw new Error(await readError(response));
        const contract = await response.json();
        $("contractSummary").textContent =
            `${contract.contractName} | Vendor: ${contract.vendor?.name || ""} | Current end date: ${contract.endDate}`;
    } catch (error) {
        $("contractSummary").textContent = error.message;
    }
}

// ---------------------------------------------------------------
// DOCUMENT REFERENCES
// ---------------------------------------------------------------

const documentForm = $("documentForm");
if (documentForm) {
    const params = new URLSearchParams(window.location.search);
    const contractId = params.get("id");

    if (!contractId) {
        showMessage($("message"), "Contract ID is missing.", true);
    } else {
        loadDocuments(contractId);

        documentForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            const message = $("message");
            const reference = $("reference").value.trim();

            try {
                const response = await fetch(`/contracts/${contractId}/documents`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ reference })
                });

                if (!response.ok) throw new Error(await readError(response));
                documentForm.reset();
                showMessage(message, "Document reference attached successfully.");
                loadDocuments(contractId);
            } catch (error) {
                showMessage(message, error.message, true);
            }
        });
    }
}

async function loadDocuments(id) {
    try {
        const contractResponse = await fetch(`/contracts/${id}`);
        if (!contractResponse.ok) throw new Error(await readError(contractResponse));
        const contract = await contractResponse.json();
        $("documentsSummary").textContent =
            `${contract.contractName} | Vendor: ${contract.vendor?.name || ""}`;

        const response = await fetch(`/contracts/${id}/documents`);
        if (!response.ok) throw new Error(await readError(response));
        const documents = await response.json();
        const body = $("documentTableBody");
        body.innerHTML = "";

        if (documents.length === 0) {
            body.innerHTML = '<tr><td colspan="3">No document references attached.</td></tr>';
            return;
        }

        documents.forEach((reference, index) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${escapeHtml(reference)}</td>
                <td><button class="button danger-button" onclick="removeDocument(${id}, ${index})">Remove</button></td>
            `;
            body.appendChild(row);
        });
    } catch (error) {
        if ($("documentsSummary")) $("documentsSummary").textContent = error.message;
    }
}

async function removeDocument(contractId, index) {
    if (!confirm("Remove this document reference?")) return;

    try {
        const response = await fetch(`/contracts/${contractId}/documents/${index}`, { method: "DELETE" });
        if (!response.ok) throw new Error(await readError(response));
        loadDocuments(contractId);
    } catch (error) {
        alert(error.message);
    }
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
