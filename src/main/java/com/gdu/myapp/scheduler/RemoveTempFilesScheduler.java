package com.gdu.myapp.scheduler;

import com.gdu.myapp.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RemoveTempFilesScheduler {
    //12시 20분에 removeTempFiles 서비스가 동작하는 스케쥴러
  private final UploadService uploadService;

  @Scheduled(cron="0 28 12 * * ?")
  public void execute() {
    uploadService.removeTempFiles();
  }


}


