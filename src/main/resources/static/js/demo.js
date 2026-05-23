const threats = [
    {
        stage: "Stage 1",
        name: "Unknown Process Detected",
        severity: "Medium",
        source: "Workstation-07",
        signal: "Unsigned executable appeared in temporary user directory",
        action: "Started behavioral scan and sandbox observation",
        level: "MEDIUM",
        confidence: 37,
        containment: 12
    },
    {
        stage: "Stage 2",
        name: "IP Spoofing Attempt",
        severity: "High",
        source: "203.0.113.77",
        signal: "Packet source mismatch and impossible session pattern",
        action: "Blocked spoofed source and flagged firewall review",
        level: "HIGH",
        confidence: 58,
        containment: 24
    },
    {
        stage: "Stage 3",
        name: "Trojan-Like Execution",
        severity: "Critical",
        source: "Workstation-07",
        signal: "Unknown executable launched from user download path",
        action: "Isolated endpoint and recommended malware scan",
        level: "CRITICAL",
        confidence: 74,
        containment: 43
    },
    {
        stage: "Stage 4",
        name: "Ransomware-Like File Burst",
        severity: "Critical",
        source: "Workstation-12",
        signal: "Rapid file rename pattern detected across user folders",
        action: "Stopped simulated process and protected shared folders",
        level: "SEVERE",
        confidence: 91,
        containment: 67
    },
    {
        stage: "Stage 4",
        name: "Credential Stuffing",
        severity: "High",
        source: "198.51.100.24",
        signal: "One IP attempted multiple usernames in short sequence",
        action: "Rate-limited source and raised identity alert",
        level: "SEVERE",
        confidence: 94,
        containment: 72
    },
    {
        stage: "Stage 5",
        name: "Phishing Link Callback",
        severity: "Medium",
        source: "Mail Gateway",
        signal: "Suspicious domain callback from user session",
        action: "Quarantined message and recommended user review",
        level: "CRITICAL",
        confidence: 87,
        containment: 81
    },
    {
        stage: "Final Stage",
        name: "Privilege Escalation Attempt",
        severity: "Critical",
        source: "admin",
        signal: "Privileged account activity followed repeated failures",
        action: "Locked account and generated incident response report",
        level: "SEVERE",
        confidence: 99,
        containment: 98
    }
];

const startButton = document.querySelector("#startDemo");
const audioToggle = document.querySelector("#audioToggle");
const threatCounter = document.querySelector("#threatCounter");
const coreStatus = document.querySelector("#coreStatus");
const simulationStatus = document.querySelector("#simulationStatus");
const threatLevel = document.querySelector("#threatLevel");
const aiConfidence = document.querySelector("#aiConfidence");
const containmentScore = document.querySelector("#containmentScore");
const activeThreat = document.querySelector("#activeThreat");
const responseConsole = document.querySelector("#responseConsole");
const threatList = document.querySelector("#threatList");
const containmentList = document.querySelector("#containmentList");
const finalOverlay = document.querySelector("#finalOverlay");
const processTable = document.querySelector("#processTable");
const streams = [
    document.querySelector("#systemStream"),
    document.querySelector("#memoryStream"),
    document.querySelector("#networkStream")
];
let contained = 0;
let running = false;
let audioEnabled = true;
let audioContext;
let droneOscillator;
let droneGain;
let streamTimer;
let streamDelay = 180;

const logFragments = {
    system: [
        "[SCAN] walking process tree: pid=4480 parent=explorer.exe",
        "[WARNING] unsigned process requested privileged handle",
        "[CHECK] registry autorun key inspected",
        "[ALERT] startup persistence pattern simulated",
        "[SCAN] file reputation unknown: unknown_process.tmp",
        "[INFO] endpoint policy synchronized",
        "[CRITICAL] suspicious process lineage detected"
    ],
    memory: [
        "[MEM] heap region entropy increased",
        "[AI] anomaly score recalculated",
        "[TRACE] simulated memory injection pattern observed",
        "[SCAN] module import table inspected",
        "[WARNING] process hollowing signature similarity: 71%",
        "[AI] pattern unfamiliar; escalating scan depth",
        "[DEFENSE] sandbox verdict pending"
    ],
    network: [
        "[NET] deep packet inspection initiated",
        "[PCAP] synthetic packet burst observed",
        "[ALERT] spoofed source pattern detected",
        "[FIREWALL] temporary block rule staged",
        "[IDS] outbound callback simulation flagged",
        "[DNS] suspicious domain reputation unknown",
        "[DEFENSE] connection contained before execution"
    ]
};

function severityClass(severity) {
    return severity.toLowerCase();
}

function writeConsole(lines) {
    lines.forEach((line, index) => {
        setTimeout(() => {
            const item = document.createElement("p");
            item.textContent = `> ${line}`;
            responseConsole.appendChild(item);
            responseConsole.scrollTop = responseConsole.scrollHeight;
        }, index * 260);
    });
}

function randomItem(items) {
    return items[Math.floor(Math.random() * items.length)];
}

function writeTerminal(stream, text) {
    if (!stream) {
        return;
    }
    const line = document.createElement("p");
    if (Math.random() > 0.82) {
        line.className = "glitch-line";
    }
    line.textContent = text;
    stream.appendChild(line);
    while (stream.children.length > 32) {
        stream.removeChild(stream.firstChild);
    }
    stream.scrollTop = stream.scrollHeight;
}

function startTerminalChaos() {
    stopTerminalChaos();
    streamTimer = setInterval(() => {
        writeTerminal(streams[0], randomItem(logFragments.system));
        writeTerminal(streams[1], randomItem(logFragments.memory));
        writeTerminal(streams[2], randomItem(logFragments.network));
    }, streamDelay);
}

function stopTerminalChaos() {
    if (streamTimer) {
        clearInterval(streamTimer);
    }
}

function setStage(threat) {
    simulationStatus.textContent = threat.stage.toUpperCase();
    threatLevel.textContent = threat.level;
    aiConfidence.textContent = `${threat.confidence}%`;
    containmentScore.textContent = `${threat.containment}%`;
    document.body.dataset.threatLevel = threat.level.toLowerCase();
    streamDelay = threat.level === "SEVERE" ? 45 : threat.level === "CRITICAL" ? 70 : threat.level === "HIGH" ? 110 : 160;
    startTerminalChaos();
}

function renderThreatCard(threat, status) {
    const card = document.createElement("div");
    card.className = "threat-card";
    card.innerHTML = `
        <span class="badge ${severityClass(threat.severity)}">${threat.severity}</span>
        <strong>${threat.name}</strong>
        <small>${threat.source}</small>
        <em>${status}</em>
    `;
    threatList.prepend(card);
}

function updateContainment(threat) {
    containmentList.innerHTML = `
        <li class="complete">Detected ${threat.name}</li>
        <li class="complete">Assigned ${threat.severity} severity</li>
        <li class="complete">${threat.action}</li>
        <li class="complete">Created SOC analyst summary</li>
    `;
}

function updateProcesses(threat) {
    const danger = threat.severity === "Critical" ? "Quarantined" : "Contained";
    processTable.innerHTML = `
        <div><span>svchost.exe</span><strong>Normal</strong></div>
        <div><span>runtime_service.exe</span><strong>Watching</strong></div>
        <div><span>system32.dll</span><strong>Verified</strong></div>
        <div><span>unknown_process.tmp</span><strong class="danger-text">${danger}</strong></div>
        <div><span>net_session.tmp</span><strong class="danger-text">Blocked</strong></div>
        <div><span>identity_cache.db</span><strong>Protected</strong></div>
    `;
}

function startAudio() {
    if (!audioEnabled) {
        return;
    }
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) {
        return;
    }
    if (!audioContext) {
        audioContext = new AudioContext();
    }
    droneOscillator = audioContext.createOscillator();
    droneGain = audioContext.createGain();
    droneOscillator.type = "sawtooth";
    droneOscillator.frequency.setValueAtTime(48, audioContext.currentTime);
    droneGain.gain.setValueAtTime(0.0001, audioContext.currentTime);
    droneGain.gain.exponentialRampToValueAtTime(0.08, audioContext.currentTime + 0.8);
    droneOscillator.connect(droneGain);
    droneGain.connect(audioContext.destination);
    droneOscillator.start();
}

function stopAudio() {
    if (droneGain && audioContext) {
        droneGain.gain.exponentialRampToValueAtTime(0.0001, audioContext.currentTime + 0.5);
    }
    if (droneOscillator) {
        setTimeout(() => droneOscillator.stop(), 650);
    }
}

function playPulse(frequency, duration) {
    if (!audioEnabled || !audioContext) {
        return;
    }
    const osc = audioContext.createOscillator();
    const gain = audioContext.createGain();
    osc.type = "triangle";
    osc.frequency.setValueAtTime(frequency, audioContext.currentTime);
    gain.gain.setValueAtTime(0.0001, audioContext.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.14, audioContext.currentTime + 0.03);
    gain.gain.exponentialRampToValueAtTime(0.0001, audioContext.currentTime + duration);
    osc.connect(gain);
    gain.connect(audioContext.destination);
    osc.start();
    osc.stop(audioContext.currentTime + duration + 0.05);
}

function runThreat(threat, index) {
    setTimeout(() => {
        setStage(threat);
        coreStatus.textContent = "Analyzing";
        document.body.classList.add("under-attack");
        activeThreat.innerHTML = `
            <span class="badge ${severityClass(threat.severity)}">${threat.severity}</span>
            <h2>${threat.stage}: ${threat.name}</h2>
            <p><strong>Source:</strong> ${threat.source}</p>
            <p>${threat.signal}</p>
        `;
        renderThreatCard(threat, "Detected");
        updateProcesses(threat);
        writeConsole([
            `AI Sentinel: telemetry received: ${threat.name}`,
            `AI Sentinel: ${threat.confidence}% confidence; ${threat.level} threat level`,
            "AI Sentinel: threat adapting; recalculating defense architecture",
            `AI Sentinel: ${threat.action}`
        ]);
        playPulse(threat.severity === "Critical" ? 92 : 140, 0.6);

        setTimeout(() => {
            contained += 1;
            threatCounter.textContent = `${contained} threats contained`;
            coreStatus.textContent = "Contained";
            document.body.classList.remove("under-attack");
            updateContainment(threat);
            renderThreatCard(threat, "Contained");
            playPulse(420, 0.25);
        }, 1900);

        if (index === threats.length - 1) {
            setTimeout(() => {
                coreStatus.textContent = "Protected";
                simulationStatus.textContent = "STABILIZED";
                threatLevel.textContent = "SECURE";
                aiConfidence.textContent = "99%";
                containmentScore.textContent = "98%";
                activeThreat.innerHTML = `
                    <span class="badge low">Demo Complete</span>
                    <h2>All simulated threats contained</h2>
                    <p>GuardianAI produced safe defensive recommendations without touching real system files.</p>
                `;
                finalOverlay.classList.add("visible");
                stopTerminalChaos();
                startButton.disabled = false;
                startButton.textContent = "Replay Simulation";
                running = false;
                stopAudio();
            }, 3300);
        }
    }, index * 3600);
}

startButton.addEventListener("click", () => {
    if (running) {
        return;
    }
    running = true;
    contained = 0;
    finalOverlay.classList.remove("visible");
    simulationStatus.textContent = "INITIALIZING";
    threatLevel.textContent = "LOW";
    aiConfidence.textContent = "0%";
    containmentScore.textContent = "0%";
    threatCounter.textContent = "0 threats contained";
    threatList.innerHTML = "";
    streams.forEach(stream => stream.innerHTML = "");
    responseConsole.innerHTML = "";
    startButton.disabled = true;
    startButton.textContent = "Simulation Running";
    startAudio();
    writeConsole(["Simulation started.", "Loading defensive playbooks.", "Monitoring simulated attack traffic."]);
    threats.forEach(runThreat);
});

audioToggle.addEventListener("click", () => {
    audioEnabled = !audioEnabled;
    audioToggle.textContent = audioEnabled ? "Audio On" : "Audio Off";
    if (!audioEnabled) {
        stopAudio();
    }
});
