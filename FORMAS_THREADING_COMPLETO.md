
| Servicio | Forma de Threading | Función |
|----------|-------------------|---------|
| `AsyncLogService` | Lambda + ExecutorService | Logging normal |
| `AsyncLogService` | Thread + Clase anónima Runnable | Logging crítico |
| `RateLimiter` | Clase anónima Runnable | Limpieza periódica |
| `ApiStatistics` | Lambda + Thread directo | Exportar estadísticas |
| `AsyncReportService` | Lambda + ExecutorService.submit | Generar informe clientes |
| `AsyncReportService` | CompletableFuture.supplyAsync | Generar informe facturas |
| `AsyncReportService` | Thread extends Thread (ClienteReportThread) | Informe con hilo dedicado |
| `AsyncReportService` | Callable con ExecutorService | Contar clientes asíncrono |
| `AsyncReportService` | ScheduledExecutorService | Limpieza periódica informes |
