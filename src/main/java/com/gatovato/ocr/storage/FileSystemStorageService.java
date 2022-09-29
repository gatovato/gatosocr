package com.gatovato.ocr.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.gatovato.ocr.exception.StorageException;
import com.gatovato.ocr.exception.StorageFileNotFoundException;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path tmpLocation;
        
        private final Path permLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.tmpLocation = Paths.get(properties.getTmp());
                this.permLocation = Paths.get(properties.getPerm());
	}

	@Override
	public void store(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.tmpLocation.resolve(
					Paths.get(file.getOriginalFilename()))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.tmpLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.tmpLocation, 1)
				.filter(path -> !path.equals(this.tmpLocation))
				.map(this.tmpLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}
        
        @Override
	public Stream<Path> loadAllTmp() {
		try {
			return Files.walk(this.tmpLocation, 1)
				.filter(path -> !path.equals(this.tmpLocation));
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return tmpLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(tmpLocation.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(tmpLocation);
                        if(!Files.exists(permLocation)){
                           Files.createDirectories(permLocation); 
                        }                        
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
        
        @Override
        public Path getPermanentLocation(){
            return permLocation;
        }
}