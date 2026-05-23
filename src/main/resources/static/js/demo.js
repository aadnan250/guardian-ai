const threats = [
    {
        name: "IP Spoofing Attempt",
        severity: "High",
        source: "203.0.113.77",
        signal: "Packet source mismatch and impossible session pattern",
        action: "Blocked spoofed source and flagged firewall review"
    },
    {
        name: "Trojan-Like Execution",
        severity: "Critical",
        source: "Workstation-07",
        signal: "Unknown executable launched from user download path",
        action: "Isolated endpoint and recommended malware scan"
    },
    {
        name: "Ransomware-Like File Burst",
        severity: "Critical",
        source: "Workstation-12",
        signal: "Rapid file rename pattern detected across user folders",
        action: "Stopped simulated process and protected shared folders"
    },
    {
        name: "Credential Stuffing",
        severity: "High",
        source: "198.51.100.24",
        signal: "One IP attempted multiple usernames in short sequence",
        action: "Rate-limited source and raised identity alert"
    },
    {
        name: "Phishing Link Callback",
        severity: "Medium",
        source: "Mail Gateway",
        signal: "Suspicious domain callback from user session",
        action: "Quarantined message and recommended user review"
    },
    {
        name: "Privilege Escalation Attempt",
        severity: "Critical",
        source: "admin",
        signal: "Privileged account activity followed repeated failures",
        action: "Locked account and generated incident response report"
    }
];

const startButton = document.querySelector("#startDemo");
const audioToggle = document.querySelector("#audioToggle");
const threatCounter = document.querySelector("#threatCounter");
const coreStatus = document.querySelector("#coreStatus");
const activeThreat = document.querySelector("#activeThreat");
const responseConsole = document.querySelector("#responseConsole");
const threatList = document.querySelector("#threatList");
const containmentList = document.querySelector("#containmentList");
let contained = 0;
let running = false;
let audioEnabled = true;
let audioContext;
let droneOscillator;
let droneGain;

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
        coreStatus.textContent = "Analyzing";
        document.body.classList.add("under-attack");
        activeThreat.innerHTML = `
            <span class="badge ${severityClass(threat.severity)}">${threat.severity}</span>
            <h2>${threat.name}</h2>
            <p><strong>Source:</strong> ${threat.source}</p>
            <p>${threat.signal}</p>
        `;
        renderThreatCard(threat, "Detected");
        writeConsole([
            `Telemetry received: ${threat.name}`,
            `Risk signal: ${threat.signal}`,
            `GuardianAI response: ${threat.action}`
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
                activeThreat.innerHTML = `
                    <span class="badge low">Demo Complete</span>
                    <h2>All simulated threats contained</h2>
                    <p>GuardianAI produced safe defensive recommendations without touching real system files.</p>
                `;
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
    threatCounter.textContent = "0 threats contained";
    threatList.innerHTML = "";
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
