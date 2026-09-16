# Security model

Running this program means **compiling and executing code that came from a question file**. That is
the core feature, so it deserves an explicit threat model rather than a disclaimer.

## What we defend against

**A hostile question pack.** The app deliberately lets anyone drop JSON files into
`data/questions/`, and each file carries Java source that gets compiled and run. A pack downloaded
from a classmate, a forum, or a course site is untrusted input.

## Two layers

### 1. Screening (`CodeSafetyScanner`)

Questions loaded from an external overlay are screened before they can run. Flagged capabilities:

| Category | Examples |
|---|---|
| process execution | `ProcessBuilder`, `Runtime.getRuntime()` |
| native code | `System.loadLibrary`, `Unsafe`, `MethodHandles` |
| filesystem writes | `Files.delete/move/write`, `FileOutputStream`, `renameTo` |
| network access | `Socket`, `ServerSocket`, `HttpClient`, `openConnection` |
| reflective override | `setAccessible(true)`, `ClassLoader`, `Class.forName` |
| environment writes | `System.setProperty`, `System.getenv` |
| code loading | `javax.tools`, `ScriptEngine`, dynamic proxies |

Comments and string literals are stripped before matching, so prose ("never call `System.exit`
here") and sample SQL or HTTP text do not trip the scan.

`System.exit` is deliberately not screened. Exercise code runs in its own subprocess, where
`exit()` ends only that subprocess — and a non-zero exit is how every test harness reports
failure, so screening it would refuse every exercise written the documented way. (It once did:
`--verify-questions` found that the shipped bank itself would have been refused.)

A flagged external question is **refused**, not run. Override with
`JAVASTUDY_TRUST_EXTERNAL=1` only for packs you wrote or reviewed yourself.

Questions bundled inside the jar are first-party: they pass the content gate in CI, so they are
trusted. An external file byte-identical to its bundled counterpart (an ordinary repo checkout)
stays trusted too — editing it makes it untrusted again, which is the correct behaviour.

Only `CODING` questions are screened, because only they are ever compiled and executed.

### 2. Containment (`Sandbox`)

The JVM that runs exercise code is wrapped in **bubblewrap** when the host has it:

- read-only bind of `/usr`, `/bin`, `/lib`, `/etc` and the JDK
- private `tmpfs` at `/tmp`; only the throwaway build directory is writable
- own PID, IPC, UTS and cgroup namespaces
- `--unshare-net`: the outside network is unreachable, while **loopback keeps working**, so the
  socket exercises still run
- `--die-with-parent` and `--new-session`

Without bubblewrap the child JVM still gets a 128 MB heap cap, a 10-second wall-clock timeout,
headless AWT and a throwaway working directory — but no kernel-enforced isolation. The startup
banner always states which mode is active. Set `JAVASTUDY_SANDBOX=off` to disable wrapping when
debugging an exercise.

Install bubblewrap for the stronger mode:

```bash
sudo apt install bubblewrap     # Debian/Ubuntu
sudo dnf install bubblewrap     # Fedora
```

## What this does not defend against

- **Your own code.** Student code you wrote runs with your privileges inside the sandbox. That is
  the point of the exercise.
- **A determined attacker with obfuscation.** The scanner is a lexical screen; reflection can be
  hidden from it. Bubblewrap is the layer that actually contains, and it is not available on every
  host.
- **Windows and macOS containment.** Neither has a bubblewrap equivalent wired up here, so those
  platforms currently get the basic protections only.
- **Progress cards as proof of work.** See `ProgressCard` — a card is evidence of self-reported
  practice. On a machine the student controls, the code is a tamper-check, not an attestation.
