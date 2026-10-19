#!/usr/bin/env python3
"""Record resolved Coherence inputs for a run, without pinning SNAPSHOT bytes."""

import hashlib
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def dependency_record(path):
    """Record actual bytes and available Maven resolution metadata as evidence."""
    artifact, version = path.parent.parent.name, path.parent.name
    pom = path.with_suffix('.pom')
    if not pom.is_file():
        pom = path.parent / f'{artifact}-{version}.pom'
    metadata = []
    for file in sorted(path.parent.glob('maven-metadata-*.xml')):
        root = ET.parse(file).getroot()
        metadata.append({'file': file.name, 'sha256': digest(file),
                         'snapshot_versions': [dict((child.tag, child.text) for child in entry)
                                               for entry in root.findall('./versioning/snapshotVersions/snapshotVersion')]})
    # Match aliases to the timestamped bytes Maven actually downloaded. Metadata
    # alone can list versions that were not used by this invocation.
    candidates = sorted(p.name for p in path.parent.glob(f'{artifact}-*.jar')
                        if p != path and p.name != f'{artifact}-{version}.jar' and digest(p) == digest(path))
    return {'path': str(path), 'artifact': artifact, 'version': version,
            'sha256': digest(path), 'pom_path': str(pom),
            'pom_sha256': digest(pom) if pom.is_file() else None,
            'matching_timestamped_files': candidates, 'maven_metadata': metadata}


def cache_files(repository, version):
    """Inventory the selected version after Maven, not a historical allowlist."""
    group = repository / 'com/oracle/coherence/ce'
    return [dependency_record(path) for path in sorted(group.glob(f'*/{version}/*.jar'))
            if path.name == f'{path.parent.parent.name}-{version}.jar']
