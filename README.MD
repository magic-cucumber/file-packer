# file-packer

[中文文档](README_CN.MD)

## 1. Introduction

file-packer is a command-line tool for file encryption and decryption. It can encrypt an entire folder into an archive file and decrypt the archive file back to its original folder structure. The program uses simple obfuscation techniques, randomly rearranging file blocks to avoid detection by cloud storage applications.

Key features:
- Cross-platform support: Supports Linux, macOS, and Windows systems
- Efficient processing: Uses Okio for high-performance file I/O operations
- Data security: Employs CBOR serialization for metadata and random rearrangement of file blocks
- Concurrent processing: Utilizes Kotlin coroutines for concurrent file processing to improve speed
- Command-line interface: Clean and intuitive command-line operations

## 2. Basic Usage

### Encrypting a Folder

To encrypt a folder into an archive file, use the following command:

```bash
./file-packer encrypt <input_folder_path> [--output <output_folder_path>] [--block-size <block_size(KB)>] [--debug]
```

Example:
```bash
./file-packer encrypt test
```

After executing the above command, a folder named test-encrypt will be generated in the current directory, containing two files:
- index.cbor: Index file storing file metadata
- payload.bin: Data file storing actual file data

### Decrypting an Archive File

To decrypt an archive file back to its original folder, use the following command:

```bash
./file-packer decrypt <archive_folder_path> [--output <output_folder_path>] [--debug]
```

Example:
```bash
./file-packer decrypt test-encrypt --output test-decrypt
```

After executing the above command, a folder named test-decrypt will be generated, containing the same file structure and content as the original folder.

## 3. Commands and Parameter Details

### encrypt Command

Used to encrypt a folder, converting it into an archive file.

Parameter description:
- `<input_folder_path>`: Path to the folder to be encrypted (required)
- `--output`: Output folder path, defaults to input folder name + "-encrypt"
- `--block-size`: Block size in KB, defaults to 128KB
- `--debug`: Enable debug logging
- `--helper`: Generates a one-click extraction script (only compatible with the same platform used for encryption).

Notes:
1. The input folder must exist and be a directory
2. The output folder must not already exist
3. Block size determines the granularity of file splitting; smaller blocks provide better data obfuscation but may increase processing time

### decrypt Command

Used to decrypt an archive file back to its original folder.

Parameter description:
- `<archive_folder_path>`: Path to the archive folder containing index.cbor and payload.bin (required)
- `--output`: Output folder path, defaults to a randomly generated UUID string
- `--debug`: Enable debug logging

Notes:
1. The archive folder must contain both index.cbor and payload.bin files
2. The output folder must not already exist
3. The decryption process requires complete index and data files; both are indispensable

### Additional Notes

- The program uses Kotlin coroutines for concurrent processing, allowing simultaneous handling of multiple file blocks to improve efficiency
- File blocks are randomly rearranged during storage, increasing data security
- Supports large file processing, avoiding memory overflow issues through chunking mechanisms
