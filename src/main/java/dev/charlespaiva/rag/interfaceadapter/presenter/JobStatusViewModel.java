package dev.charlespaiva.rag.interfaceadapter.presenter;

import java.util.UUID;

public record JobStatusViewModel(UUID jobId, String status, String createdAt, String failureReason) {
}
