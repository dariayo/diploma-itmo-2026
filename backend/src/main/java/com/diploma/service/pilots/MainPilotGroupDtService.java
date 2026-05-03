package com.diploma.service.pilots;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.diploma.entities.MainPilotGroupDt;
import com.diploma.repository.MainPilotGroupDtRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class MainPilotGroupDtService {
    private final MainPilotGroupDtRepository mainPilotGroupDtRepository;

    public List<MainPilotGroupDt> getByDataRange(LocalDateTime start, LocalDateTime finish) {
        return  mainPilotGroupDtRepository.findByDtBetween(start, finish);
    }
}
