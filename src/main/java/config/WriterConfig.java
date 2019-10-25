package config;

import annotations.Blueprint;
import annotations.Definition;
import service.WriterService;

@Blueprint
public class WriterConfig {

    @Definition
    public WriterService writerService() {
        return new WriterService("Hello, World!");
    }
}
