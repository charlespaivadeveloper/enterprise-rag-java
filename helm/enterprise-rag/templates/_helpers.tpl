{{/*
Fully qualified app name (max 63 chars).
If the release name already contains the chart name the suffix is omitted
to avoid redundant names like "enterprise-rag-enterprise-rag".
*/}}
{{- define "enterprise-rag.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
PostgreSQL fully qualified name.
*/}}
{{- define "enterprise-rag.postgres.fullname" -}}
{{- printf "%s-postgres" (include "enterprise-rag.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Secret name.
*/}}
{{- define "enterprise-rag.secretName" -}}
{{- printf "%s-secrets" (include "enterprise-rag.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels applied to every resource.
*/}}
{{- define "enterprise-rag.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels (stable — used by Deployments and Services).
*/}}
{{- define "enterprise-rag.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
