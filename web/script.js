let currentProcessCount = 0;
let currentResourceCount = 0;

function generateMatrices() {
    const processCount = Number.parseInt(document.getElementById('numProcesses').value, 10);
    const resourceCount = Number.parseInt(document.getElementById('numResources').value, 10);

    if (isNaN(processCount) || processCount < 1 || processCount > 20) {
        showError('Number of Processes must be between 1 and 20.');
        return;
    }

    if (isNaN(resourceCount) || resourceCount < 1 || resourceCount > 10) {
        showError('Number of Resource Types must be between 1 and 10.');
        return;
    }

    currentProcessCount = processCount;
    currentResourceCount = resourceCount;

    const totalGrid = document.getElementById('totalResourcesGrid');
    totalGrid.innerHTML = '';
    for (let j = 0; j < resourceCount; j++) {
        const cell = document.createElement('div');
        cell.className = 'matrix-cell';
        cell.innerHTML = `
            <label for="total_${j}">R${j}</label>
            <input type="number" class="matrix-input" id="total_${j}" min="0" value="0" placeholder="0">
        `;
        totalGrid.appendChild(cell);
    }

    buildMatrixTable('allocationMatrix', 'alloc', processCount, resourceCount);
    buildMatrixTable('requestMatrix', 'req', processCount, resourceCount);

    document.getElementById('matricesContainer').style.display = 'block';
    document.getElementById('resultsContainer').style.display = 'none';
    document.getElementById('step2').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function buildMatrixTable(containerId, inputPrefix, rows, cols) {
    const container = document.getElementById(containerId);

    let html = '<table class="matrix-table"><thead><tr><th></th>';
    for (let j = 0; j < cols; j++) {
        html += `<th>R${j}</th>`;
    }
    html += '</tr></thead><tbody>';

    for (let i = 0; i < rows; i++) {
        html += `<tr><td>P${i}</td>`;
        for (let j = 0; j < cols; j++) {
            html += `<td><input type="number" class="matrix-input" id="${inputPrefix}_${i}_${j}" min="0" value="0" placeholder="0"></td>`;
        }
        html += '</tr>';
    }

    html += '</tbody></table>';
    container.innerHTML = html;
}

async function runAlgorithm() {
    const n = currentProcessCount;
    const m = currentResourceCount;

    if (n === 0 || m === 0) {
        showError('Please generate the matrices first before running the algorithm.');
        return;
    }

    const totalResources = [];
    for (let j = 0; j < m; j++) {
        const value = Number.parseInt(document.getElementById(`total_${j}`).value, 10);
        if (isNaN(value) || value < 0) {
            showError(`Total Resource R${j} must be a non-negative number.`);
            return;
        }
        totalResources.push(value);
    }

    const allocation = [];
    for (let i = 0; i < n; i++) {
        const row = [];
        for (let j = 0; j < m; j++) {
            const value = Number.parseInt(document.getElementById(`alloc_${i}_${j}`).value, 10);
            if (isNaN(value) || value < 0) {
                showError(`Allocation[P${i}][R${j}] must be a non-negative number.`);
                return;
            }
            row.push(value);
        }
        allocation.push(row);
    }

    const request = [];
    for (let i = 0; i < n; i++) {
        const row = [];
        for (let j = 0; j < m; j++) {
            const value = Number.parseInt(document.getElementById(`req_${i}_${j}`).value, 10);
            if (isNaN(value) || value < 0) {
                showError(`Request[P${i}][R${j}] must be a non-negative number.`);
                return;
            }
            row.push(value);
        }
        request.push(row);
    }

    document.getElementById('loadingSpinner').style.display = 'block';
    document.getElementById('resultsContainer').style.display = 'none';
    document.getElementById('loadingSpinner').scrollIntoView({ behavior: 'smooth', block: 'center' });

    try {
        const response = await fetch('http://localhost:8080/api/solve', {
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

        document.getElementById('loadingSpinner').style.display = 'none';
        renderResults(data);
    } catch (error) {
        document.getElementById('loadingSpinner').style.display = 'none';
        showError('Could not connect to the server. Make sure the Java backend is running on port 8080.');
    }
}

function renderResults(data) {
    const container = document.getElementById('resultsContainer');
    container.style.display = 'block';

    const availableDisplay = document.getElementById('availableVector');
    availableDisplay.innerHTML = '';
    data.available.forEach((value, index) => {
        availableDisplay.innerHTML += `
            <div class="vector-item" role="listitem">
                <span class="v-label">R${index}</span>
                <span class="v-value">${value}</span>
            </div>
        `;
    });

    if (data.allocation && data.request) {
        renderResourceAllocationGraph(data);
    }



    const resultCard = document.getElementById('resultCard');
    const resultBadge = document.getElementById('resultBadge');
    const resultTitle = document.getElementById('resultTitle');
    const resultBody = document.getElementById('resultBody');

    resultCard.classList.remove('result-safe', 'result-deadlock');
    resultTitle.textContent = 'System Analysis Result';

    if (data.isSafe) {
        resultCard.classList.add('result-safe');
        resultBadge.className = 'step-badge badge-safe';
        resultBadge.textContent = 'SAFE';

        let html = '<div class="result-icon">OK</div>';
        html += '<div class="result-message safe">SYSTEM IS IN A SAFE STATE</div>';
        html += '<p class="result-sub">All processes can complete without deadlock.</p>';
        html += '<div class="sequence-display">';

        data.safeSequence.forEach((proc, index) => {
            const delay = index * 0.1;
            html += `<span class="seq-process safe" style="animation-delay: ${delay}s">P${proc}</span>`;
            if (index < data.safeSequence.length - 1) {
                html += `<span class="seq-arrow" style="animation-delay: ${delay + 0.05}s">-&gt;</span>`;
            }
        });

        html += '</div>';
        resultBody.innerHTML = html;
    } else {
        resultCard.classList.add('result-deadlock');
        resultBadge.className = 'step-badge badge-deadlock';
        resultBadge.textContent = 'DEADLOCK';

        let html = '<div class="result-icon">X</div>';
        html += '<div class="result-message deadlock">DEADLOCK DETECTED</div>';
        html += '<p class="result-sub">The following processes are blocked.</p>';
        html += '<div class="sequence-display">';

        data.deadlockedProcs.forEach((proc, index) => {
            const delay = index * 0.1;
            html += `<span class="seq-process deadlocked" style="animation-delay: ${delay}s">P${proc}</span>`;
        });

        html += '</div>';
        resultBody.innerHTML = html;
    }

    setTimeout(() => {
        document.getElementById('availableCard').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 200);
}

function renderResourceAllocationGraph(data) {
    const container = document.getElementById('ragContainer');
    const n = data.n;
    const m = data.m;
    const allocation = data.allocation;
    const request = data.request;

    const nodeRadius = 28;
    const resourceBoxSize = 44;
    const padding = 60;
    const columnGap = 320;
    const processRowSpacing = Math.max(80, 440 / Math.max(n, 1));
    const resourceRowSpacing = Math.max(80, 440 / Math.max(m, 1));

    const processColumnX = padding + nodeRadius;
    const resourceColumnX = padding + nodeRadius + columnGap;
    const svgWidth = resourceColumnX + nodeRadius + padding;
    const totalHeight = Math.max(n * processRowSpacing, m * resourceRowSpacing) + padding * 2;
    const svgHeight = Math.max(totalHeight, 300);

    const processStartY = (svgHeight - (n - 1) * processRowSpacing) / 2;
    const resourceStartY = (svgHeight - (m - 1) * resourceRowSpacing) / 2;

    const processNodes = [];
    for (let i = 0; i < n; i++) {
        processNodes.push({ x: processColumnX, y: processStartY + i * processRowSpacing });
    }

    const resourceNodes = [];
    for (let j = 0; j < m; j++) {
        resourceNodes.push({ x: resourceColumnX, y: resourceStartY + j * resourceRowSpacing });
    }

    const namespace = 'http://www.w3.org/2000/svg';
    let svg = `<svg xmlns="${namespace}" width="${svgWidth}" height="${svgHeight}" viewBox="0 0 ${svgWidth} ${svgHeight}">`;

    svg += `<defs>
        <marker id="arrowAlloc" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
            <polygon points="0 0, 10 4, 0 8" fill="#22c55e"/>
        </marker>
        <marker id="arrowReq" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
            <polygon points="0 0, 10 4, 0 8" fill="#ef4444"/>
        </marker>
    </defs>`;

    for (let i = 0; i < n; i++) {
        for (let j = 0; j < m; j++) {
            if (allocation[i][j] > 0) {
                const px = processNodes[i].x + nodeRadius;
                const py = processNodes[i].y;
                const rx = resourceNodes[j].x - resourceBoxSize / 2;
                const ry = resourceNodes[j].y;
                const cx1 = rx - 60;
                const cx2 = px + 60;

                svg += `<path d="M ${rx} ${ry} C ${cx1} ${ry}, ${cx2} ${py}, ${px} ${py}"
                    fill="none" stroke="#22c55e" stroke-width="2" marker-end="url(#arrowAlloc)" opacity="0.8"/>`;

                const midX = (rx + px) / 2;
                const midY = (ry + py) / 2 - 8;
                svg += `<text x="${midX}" y="${midY}" fill="#22c55e" font-size="12"
                    font-family="'JetBrains Mono', monospace" font-weight="600"
                    text-anchor="middle">${allocation[i][j]}</text>`;
            }
        }
    }

    for (let i = 0; i < n; i++) {
        for (let j = 0; j < m; j++) {
            if (request[i][j] > 0) {
                const px = processNodes[i].x + nodeRadius;
                const py = processNodes[i].y;
                const rx = resourceNodes[j].x - resourceBoxSize / 2;
                const ry = resourceNodes[j].y;
                const cx1 = px + 80;
                const cx2 = rx - 80;

                svg += `<path d="M ${px} ${py} C ${cx1} ${py}, ${cx2} ${ry}, ${rx} ${ry}"
                    fill="none" stroke="#ef4444" stroke-width="2" stroke-dasharray="6 3"
                    marker-end="url(#arrowReq)" opacity="0.7"/>`;

                const midX = (rx + px) / 2;
                const midY = (ry + py) / 2 + 14;
                svg += `<text x="${midX}" y="${midY}" fill="#ef4444" font-size="12"
                    font-family="'JetBrains Mono', monospace" font-weight="600"
                    text-anchor="middle">${request[i][j]}</text>`;
            }
        }
    }

    for (let i = 0; i < n; i++) {
        const { x, y } = processNodes[i];
        const isDeadlocked = data.deadlockedProcs && data.deadlockedProcs.includes(i);
        const fillColor = isDeadlocked ? 'rgba(239,68,68,0.15)' : 'rgba(6,182,212,0.15)';
        const strokeColor = isDeadlocked ? '#ef4444' : '#06b6d4';
        svg += `<circle cx="${x}" cy="${y}" r="${nodeRadius}" fill="${fillColor}"
            stroke="${strokeColor}" stroke-width="2.5"/>`;
        svg += `<text x="${x}" y="${y + 5}" fill="${strokeColor}" font-size="14"
            font-family="'JetBrains Mono', monospace" font-weight="700"
            text-anchor="middle">P${i}</text>`;
    }

    for (let j = 0; j < m; j++) {
        const { x, y } = resourceNodes[j];
        svg += `<rect x="${x - resourceBoxSize / 2}" y="${y - resourceBoxSize / 2}" width="${resourceBoxSize}"
            height="${resourceBoxSize}" rx="6" fill="rgba(139,92,246,0.15)"
            stroke="#8b5cf6" stroke-width="2.5"/>`;
        svg += `<text x="${x}" y="${y + 5}" fill="#8b5cf6" font-size="14"
            font-family="'JetBrains Mono', monospace" font-weight="700"
            text-anchor="middle">R${j}</text>`;
        if (data.totalResources) {
            svg += `<text x="${x}" y="${y + resourceBoxSize / 2 + 16}" fill="#94a3b8" font-size="10"
                font-family="'Inter', sans-serif" font-weight="500"
                text-anchor="middle">Total: ${data.totalResources[j]}</text>`;
        }
    }

    svg += '</svg>';
    container.innerHTML = svg;
}

function resetAll() {
    document.getElementById('matricesContainer').style.display = 'none';
    document.getElementById('resultsContainer').style.display = 'none';
    document.getElementById('loadingSpinner').style.display = 'none';

    currentProcessCount = 0;
    currentResourceCount = 0;

    document.getElementById('hero').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function showError(message) {
    const existing = document.querySelector('.error-toast');
    if (existing) {
        existing.remove();
    }

    const toast = document.createElement('div');
    toast.className = 'error-toast';
    toast.innerHTML = `
        <span style="margin-right: 8px;">!</span>
        <span>${message}</span>
        <button type="button" aria-label="Dismiss error">&times;</button>
    `;

    const closeButton = toast.querySelector('button');
    closeButton.addEventListener('click', () => {
        toast.remove();
    });

    document.body.appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(-50%) translateY(-10px)';
            toast.style.transition = 'all 0.3s ease-out';
            setTimeout(() => toast.remove(), 300);
        }
    }, 5000);
}
