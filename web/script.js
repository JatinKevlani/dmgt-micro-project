/* ═══════════════════════════════════════════════════════════════
   DEADLOCK DETECTION SIMULATOR — Frontend Logic
   Dynamic matrix generation, AJAX, result rendering
   ═══════════════════════════════════════════════════════════════ */

// ─── State Variables ───────────────────────────────────────────
let currentN = 0;
let currentM = 0;

// ─── Generate Matrices ─────────────────────────────────────────
function generateMatrices() {
    const n = parseInt(document.getElementById('numProcesses').value);
    const m = parseInt(document.getElementById('numResources').value);

    // Validate
    if (isNaN(n) || n < 1 || n > 20) {
        showError('Number of Processes must be between 1 and 20.');
        return;
    }
    if (isNaN(m) || m < 1 || m > 10) {
        showError('Number of Resource Types must be between 1 and 10.');
        return;
    }

    currentN = n;
    currentM = m;

    // Generate Total Resources grid
    const totalGrid = document.getElementById('totalResourcesGrid');
    totalGrid.innerHTML = '';
    for (let j = 0; j < m; j++) {
        const cell = document.createElement('div');
        cell.className = 'matrix-cell';
        cell.innerHTML = `
            <label>R${j}</label>
            <input type="number" class="matrix-input" id="total_${j}" min="0" value="0" placeholder="0">
        `;
        totalGrid.appendChild(cell);
    }

    // Generate Allocation Matrix
    buildMatrixTable('allocationMatrix', 'alloc', n, m);

    // Generate Request Matrix
    buildMatrixTable('requestMatrix', 'req', n, m);

    // Show matrices section
    document.getElementById('matricesContainer').style.display = 'block';
    document.getElementById('resultsContainer').style.display = 'none';

    // Smooth scroll to step 2
    document.getElementById('step2').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ─── Build Matrix Table ────────────────────────────────────────
function buildMatrixTable(containerId, prefix, n, m) {
    const container = document.getElementById(containerId);

    let html = '<table class="matrix-table"><thead><tr><th></th>';
    for (let j = 0; j < m; j++) {
        html += `<th>R${j}</th>`;
    }
    html += '</tr></thead><tbody>';

    for (let i = 0; i < n; i++) {
        html += `<tr><td>P${i}</td>`;
        for (let j = 0; j < m; j++) {
            html += `<td><input type="number" class="matrix-input" id="${prefix}_${i}_${j}" min="0" value="0" placeholder="0"></td>`;
        }
        html += '</tr>';
    }
    html += '</tbody></table>';
    container.innerHTML = html;
}

// ─── Load Safe State Test Case ─────────────────────────────────
function loadSafeTestCase() {
    // Set dimensions
    document.getElementById('numProcesses').value = 5;
    document.getElementById('numResources').value = 3;
    generateMatrices();

    // Total Resources: [10, 5, 7]
    const total = [10, 5, 7];
    total.forEach((v, j) => document.getElementById(`total_${j}`).value = v);

    // Allocation Matrix
    const alloc = [
        [0, 1, 0],
        [2, 0, 0],
        [3, 0, 2],
        [2, 1, 1],
        [0, 0, 2]
    ];
    alloc.forEach((row, i) => row.forEach((v, j) => document.getElementById(`alloc_${i}_${j}`).value = v));

    // Request Matrix
    const req = [
        [7, 4, 3],
        [1, 2, 2],
        [6, 0, 0],
        [0, 1, 1],
        [4, 3, 1]
    ];
    req.forEach((row, i) => row.forEach((v, j) => document.getElementById(`req_${i}_${j}`).value = v));
}

// ─── Load Deadlock Test Case ───────────────────────────────────
function loadDeadlockTestCase() {
    // Set dimensions
    document.getElementById('numProcesses').value = 3;
    document.getElementById('numResources').value = 2;
    generateMatrices();

    // Total Resources: [2, 2]
    const total = [2, 2];
    total.forEach((v, j) => document.getElementById(`total_${j}`).value = v);

    // Allocation Matrix
    const alloc = [
        [1, 1],
        [1, 0],
        [0, 1]
    ];
    alloc.forEach((row, i) => row.forEach((v, j) => document.getElementById(`alloc_${i}_${j}`).value = v));

    // Request Matrix
    const req = [
        [1, 1],
        [1, 1],
        [1, 0]
    ];
    req.forEach((row, i) => row.forEach((v, j) => document.getElementById(`req_${i}_${j}`).value = v));
}

// ─── Run Algorithm ─────────────────────────────────────────────
async function runAlgorithm() {
    const n = currentN;
    const m = currentM;

    if (n === 0 || m === 0) {
        showError('Please generate matrices first.');
        return;
    }

    // Collect Total Resources
    const totalResources = [];
    for (let j = 0; j < m; j++) {
        const val = parseInt(document.getElementById(`total_${j}`).value);
        if (isNaN(val) || val < 0) {
            showError(`Total Resource R${j} must be a non-negative integer.`);
            return;
        }
        totalResources.push(val);
    }

    // Collect Allocation Matrix
    const allocation = [];
    for (let i = 0; i < n; i++) {
        const row = [];
        for (let j = 0; j < m; j++) {
            const val = parseInt(document.getElementById(`alloc_${i}_${j}`).value);
            if (isNaN(val) || val < 0) {
                showError(`Allocation[P${i}][R${j}] must be a non-negative integer.`);
                return;
            }
            row.push(val);
        }
        allocation.push(row);
    }

    // Collect Request Matrix
    const request = [];
    for (let i = 0; i < n; i++) {
        const row = [];
        for (let j = 0; j < m; j++) {
            const val = parseInt(document.getElementById(`req_${i}_${j}`).value);
            if (isNaN(val) || val < 0) {
                showError(`Request[P${i}][R${j}] must be a non-negative integer.`);
                return;
            }
            row.push(val);
        }
        request.push(row);
    }

    // Show loading
    document.getElementById('loadingSpinner').style.display = 'block';
    document.getElementById('resultsContainer').style.display = 'none';
    document.getElementById('loadingSpinner').scrollIntoView({ behavior: 'smooth', block: 'center' });

    try {
        const response = await fetch('/api/solve', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ n, m, totalResources, allocation, request })
        });

        const data = await response.json();

        if (data.error) {
            showError(data.error);
            document.getElementById('loadingSpinner').style.display = 'none';
            return;
        }

        // Hide loading, show results
        document.getElementById('loadingSpinner').style.display = 'none';
        renderResults(data);

    } catch (err) {
        document.getElementById('loadingSpinner').style.display = 'none';
        showError('Failed to connect to the server. Make sure the Java server is running.');
    }
}

// ─── Render Results ────────────────────────────────────────────
function renderResults(data) {
    const container = document.getElementById('resultsContainer');
    container.style.display = 'block';

    // ── Available Vector ──
    const availVec = document.getElementById('availableVector');
    availVec.innerHTML = '';
    data.available.forEach((val, j) => {
        availVec.innerHTML += `
            <div class="vector-item">
                <span class="v-label">R${j}</span>
                <span class="v-value">${val}</span>
            </div>
        `;
    });

    // ── Resource Allocation Graph ──
    if (data.allocation && data.request) {
        renderRAG(data);
    }

    // ── Trace Log ──
    const traceDiv = document.getElementById('traceContainer');
    traceDiv.innerHTML = '';
    data.traceLog.forEach(line => {
        const div = document.createElement('div');
        div.className = 'trace-line';

        if (line.includes('ITERATION')) {
            div.classList.add('trace-iteration');
        } else if (line.includes('✔ EXECUTE')) {
            div.classList.add('trace-execute');
        } else if (line.includes('✗ SKIP')) {
            div.classList.add('trace-skip');
        } else if (line.includes('No process can proceed')) {
            div.classList.add('trace-blocked');
        } else if (line.startsWith('─') || line.startsWith('  ─')) {
            div.classList.add('trace-separator');
        } else {
            div.classList.add('trace-info');
        }

        div.textContent = line;
        traceDiv.appendChild(div);
    });

    // ── Result Card ──
    const resultCard = document.getElementById('resultCard');
    const resultBadge = document.getElementById('resultBadge');
    const resultTitle = document.getElementById('resultTitle');
    const resultBody = document.getElementById('resultBody');

    resultCard.classList.remove('result-safe', 'result-deadlock');

    if (data.isSafe) {
        // ── SAFE STATE ──
        resultCard.classList.add('result-safe');
        resultBadge.className = 'step-badge badge-safe';
        resultBadge.textContent = 'SAFE';
        resultTitle.textContent = 'System Analysis Result';

        let seqHtml = '<div class="result-icon">✅</div>';
        seqHtml += '<div class="result-message safe">SYSTEM IS IN A SAFE STATE</div>';
        seqHtml += '<p class="result-sub">All processes can complete without deadlock.</p>';
        seqHtml += '<div class="sequence-display">';

        data.safeSequence.forEach((proc, idx) => {
            const delay = idx * 0.1;
            seqHtml += `<span class="seq-process safe" style="animation-delay: ${delay}s">P${proc}</span>`;
            if (idx < data.safeSequence.length - 1) {
                seqHtml += `<span class="seq-arrow" style="animation-delay: ${delay + 0.05}s">→</span>`;
            }
        });

        seqHtml += '</div>';
        resultBody.innerHTML = seqHtml;

    } else {
        // ── DEADLOCK ──
        resultCard.classList.add('result-deadlock');
        resultBadge.className = 'step-badge badge-deadlock';
        resultBadge.textContent = 'DEADLOCK';
        resultTitle.textContent = 'System Analysis Result';

        let deadHtml = '<div class="result-icon">❌</div>';
        deadHtml += '<div class="result-message deadlock">DEADLOCK DETECTED!</div>';
        deadHtml += '<p class="result-sub">The following processes are permanently blocked and will wait indefinitely.</p>';
        deadHtml += '<div class="sequence-display">';

        data.deadlockedProcs.forEach((proc, idx) => {
            const delay = idx * 0.1;
            deadHtml += `<span class="seq-process deadlocked" style="animation-delay: ${delay}s">P${proc}</span>`;
        });

        deadHtml += '</div>';
        resultBody.innerHTML = deadHtml;
    }

    // Scroll to results
    setTimeout(() => {
        document.getElementById('availableCard').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 200);
}

// ─── Render Resource Allocation Graph (SVG) ────────────────────
function renderRAG(data) {
    const container = document.getElementById('ragContainer');
    const n = data.n;
    const m = data.m;
    const allocation = data.allocation;
    const request = data.request;

    // Layout constants
    const nodeRadius = 28;
    const resSize = 44;
    const padding = 60;
    const colGap = 320;
    const rowGapP = Math.max(80, 440 / Math.max(n, 1));
    const rowGapR = Math.max(80, 440 / Math.max(m, 1));

    const leftX = padding + nodeRadius;
    const rightX = padding + nodeRadius + colGap;
    const svgWidth = rightX + nodeRadius + padding;

    // Compute Y positions centered vertically
    const maxRows = Math.max(n, m);
    const totalHeight = Math.max(n * rowGapP, m * rowGapR) + padding * 2;
    const svgHeight = Math.max(totalHeight, 300);

    const processYStart = (svgHeight - (n - 1) * rowGapP) / 2;
    const resourceYStart = (svgHeight - (m - 1) * rowGapR) / 2;

    const processPositions = [];
    for (let i = 0; i < n; i++) {
        processPositions.push({ x: leftX, y: processYStart + i * rowGapP });
    }

    const resourcePositions = [];
    for (let j = 0; j < m; j++) {
        resourcePositions.push({ x: rightX, y: resourceYStart + j * rowGapR });
    }

    // SVG namespace
    const NS = 'http://www.w3.org/2000/svg';

    // Build SVG element
    let svg = `<svg xmlns="${NS}" width="${svgWidth}" height="${svgHeight}" viewBox="0 0 ${svgWidth} ${svgHeight}">`;

    // Defs for arrow markers
    svg += `<defs>
        <marker id="arrowAlloc" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
            <polygon points="0 0, 10 4, 0 8" fill="#22c55e"/>
        </marker>
        <marker id="arrowReq" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
            <polygon points="0 0, 10 4, 0 8" fill="#ef4444"/>
        </marker>
    </defs>`;

    // ── Draw Edges ──
    // Allocation: Resource → Process (solid green)
    for (let i = 0; i < n; i++) {
        for (let j = 0; j < m; j++) {
            if (allocation[i][j] > 0) {
                const px = processPositions[i].x + nodeRadius;
                const py = processPositions[i].y;
                const rx = resourcePositions[j].x - resSize / 2;
                const ry = resourcePositions[j].y;
                // Curved bezier from resource to process
                const cx1 = rx - 60;
                const cx2 = px + 60;
                svg += `<path d="M ${rx} ${ry} C ${cx1} ${ry}, ${cx2} ${py}, ${px} ${py}"
                    fill="none" stroke="#22c55e" stroke-width="2" marker-end="url(#arrowAlloc)"
                    opacity="0.8"/>`;
                // Label
                const midX = (rx + px) / 2;
                const midY = (ry + py) / 2 - 8;
                svg += `<text x="${midX}" y="${midY}" fill="#22c55e" font-size="12"
                    font-family="'JetBrains Mono', monospace" font-weight="600"
                    text-anchor="middle">${allocation[i][j]}</text>`;
            }
        }
    }

    // Request: Process → Resource (dashed red)
    for (let i = 0; i < n; i++) {
        for (let j = 0; j < m; j++) {
            if (request[i][j] > 0) {
                const px = processPositions[i].x + nodeRadius;
                const py = processPositions[i].y;
                const rx = resourcePositions[j].x - resSize / 2;
                const ry = resourcePositions[j].y;
                // Curved bezier from process to resource
                const cx1 = px + 80;
                const cx2 = rx - 80;
                svg += `<path d="M ${px} ${py} C ${cx1} ${py}, ${cx2} ${ry}, ${rx} ${ry}"
                    fill="none" stroke="#ef4444" stroke-width="2" stroke-dasharray="6 3"
                    marker-end="url(#arrowReq)" opacity="0.7"/>`;
                // Label
                const midX = (rx + px) / 2;
                const midY = (ry + py) / 2 + 14;
                svg += `<text x="${midX}" y="${midY}" fill="#ef4444" font-size="12"
                    font-family="'JetBrains Mono', monospace" font-weight="600"
                    text-anchor="middle">${request[i][j]}</text>`;
            }
        }
    }

    // ── Draw Process Nodes (circles) ──
    for (let i = 0; i < n; i++) {
        const { x, y } = processPositions[i];
        const isDeadlocked = data.deadlockedProcs && data.deadlockedProcs.includes(i);
        const fillColor = isDeadlocked ? 'rgba(239,68,68,0.15)' : 'rgba(6,182,212,0.15)';
        const strokeColor = isDeadlocked ? '#ef4444' : '#06b6d4';
        svg += `<circle cx="${x}" cy="${y}" r="${nodeRadius}" fill="${fillColor}"
            stroke="${strokeColor}" stroke-width="2.5"/>`;
        svg += `<text x="${x}" y="${y + 5}" fill="${strokeColor}" font-size="14"
            font-family="'JetBrains Mono', monospace" font-weight="700"
            text-anchor="middle">P${i}</text>`;
    }

    // ── Draw Resource Nodes (rectangles) ──
    for (let j = 0; j < m; j++) {
        const { x, y } = resourcePositions[j];
        svg += `<rect x="${x - resSize / 2}" y="${y - resSize / 2}" width="${resSize}"
            height="${resSize}" rx="6" fill="rgba(139,92,246,0.15)"
            stroke="#8b5cf6" stroke-width="2.5"/>`;
        svg += `<text x="${x}" y="${y + 5}" fill="#8b5cf6" font-size="14"
            font-family="'JetBrains Mono', monospace" font-weight="700"
            text-anchor="middle">R${j}</text>`;
        // Show total count below
        if (data.totalResources) {
            svg += `<text x="${x}" y="${y + resSize / 2 + 16}" fill="#94a3b8" font-size="10"
                font-family="'Inter', sans-serif" font-weight="500"
                text-anchor="middle">Total: ${data.totalResources[j]}</text>`;
        }
    }

    svg += '</svg>';
    container.innerHTML = svg;
}

// ─── Reset All ─────────────────────────────────────────────────
function resetAll() {
    document.getElementById('matricesContainer').style.display = 'none';
    document.getElementById('resultsContainer').style.display = 'none';
    document.getElementById('loadingSpinner').style.display = 'none';

    currentN = 0;
    currentM = 0;

    // Scroll to top
    document.getElementById('hero').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ─── Error Display ─────────────────────────────────────────────
function showError(message) {
    // Create a floating error notification
    const existing = document.querySelector('.error-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = 'error-toast';
    toast.innerHTML = `
        <span style="margin-right: 8px;">⚠️</span>
        <span>${message}</span>
        <button onclick="this.parentElement.remove()" style="
            background: none; border: none; color: inherit; cursor: pointer;
            font-size: 18px; margin-left: 12px; padding: 0; opacity: 0.7;
        ">&times;</button>
    `;

    // Style the toast
    Object.assign(toast.style, {
        position: 'fixed',
        top: '24px',
        left: '50%',
        transform: 'translateX(-50%)',
        background: 'rgba(239, 68, 68, 0.95)',
        color: 'white',
        padding: '14px 24px',
        borderRadius: '12px',
        fontSize: '14px',
        fontFamily: "'Inter', sans-serif",
        fontWeight: '500',
        zIndex: '9999',
        display: 'flex',
        alignItems: 'center',
        backdropFilter: 'blur(10px)',
        boxShadow: '0 8px 32px rgba(239, 68, 68, 0.4)',
        animation: 'fadeInUp 0.3s ease-out',
        maxWidth: '90vw'
    });

    document.body.appendChild(toast);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(-50%) translateY(-10px)';
            toast.style.transition = 'all 0.3s ease-out';
            setTimeout(() => toast.remove(), 300);
        }
    }, 5000);
}
