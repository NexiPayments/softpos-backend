package com.example.pizzapaybackend.services;

import com.example.pizzapaybackend.configfile.AvailablePointOfSale;
import com.example.pizzapaybackend.configfile.AvailablePointOfSaleTerminals;
import com.example.pizzapaybackend.configfile.NexiSoftPosBackendConfig;
import com.example.pizzapaybackend.configfile.PointOfSalesConfig;
import com.example.pizzapaybackend.entities.UserEntity;
import com.example.pizzapaybackend.pojo.AndroidAppUserConfig;
import com.example.pizzapaybackend.repositories.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AndroidAppConfigService {

    private final NexiSoftPosBackendConfig nexiSoftPosBackendConfig;

    private final UserRepository userRepository;

    private final PointOfSalesConfig pointOfSalesConfig;

    public String getClientId() {
        return nexiSoftPosBackendConfig.getClientId();
    }

    public Optional<AndroidAppUserConfig> setupUser(
        final UserEntity userEntity
    ) {
        /*
         * If the user doesn't have the point of sale and the tid associated our backend will find the first
         * availbale and will assign it to the user
         * 
         * Each user must have a dedicated tid to avoid conflics
         */
        if (userEntity.getPointOfSale() == null && userEntity.getTerminalIdSoftpos() == null && userEntity.getTerminalIdMpos() == null) {
            final Optional<FirstAvailableData> optionalFirstAvailableData = findFirstAvailableData();
            if (optionalFirstAvailableData.isPresent()) {
                userEntity.setPointOfSale(optionalFirstAvailableData.get().pointOfSale());
                userEntity.setTerminalIdSoftpos(optionalFirstAvailableData.get().terminalIdSoftpos());
                userEntity.setTerminalIdMpos(optionalFirstAvailableData.get().terminalIdMpos());
            } else {
                return Optional.empty();
            }
        }

        log.info(
            "userEntity with point of sale {} with tid {}",
            userEntity.getPointOfSale(),
            userEntity.getTerminalIdSoftpos()
        );
        userRepository.save(userEntity);

        return Optional.of(new AndroidAppUserConfig(userEntity.getPointOfSale(), userEntity.getTerminalIdSoftpos(), userEntity.getTerminalIdMpos()));
    }


    private static record FirstAvailableData(
        String pointOfSale,
        String terminalIdSoftpos,
        String terminalIdMpos
    ) {
    }

    private Optional<FirstAvailableData> findFirstAvailableData() {
        if (pointOfSalesConfig.getAvailable() == null) {
            log.error("point of sales config is null");
            return Optional.empty();
        }
        /**
         * we check each point of sale and each terminal searching for the first available
         */
        for (final AvailablePointOfSale pointOfSaleInfo : pointOfSalesConfig.getAvailable()) {
            for (final AvailablePointOfSaleTerminals tid : pointOfSaleInfo.getTerminals()) {
                final boolean isAssignedSoftpos = userRepository.exists(
                    (root, query, criteriaBuilder) -> criteriaBuilder.and(
                        criteriaBuilder.equal(
                            root.get("pointOfSale"),
                            pointOfSaleInfo.getPointOfSale()
                        ),
                        criteriaBuilder.equal(
                            root.get("terminalIdSoftpos"),
                            tid.getForSoftpos()
                        )
                    )
                );
                final boolean isAssignedMpos = userRepository.exists(
                    (root, query, criteriaBuilder) -> criteriaBuilder.and(
                        criteriaBuilder.equal(
                            root.get("pointOfSale"),
                            pointOfSaleInfo.getPointOfSale()
                        ),
                        criteriaBuilder.equal(
                            root.get("terminalIdMpos"),
                            tid.getForMpos()
                        )
                    )
                );
                if (!(isAssignedSoftpos || isAssignedMpos)) {
                    return Optional.of(new FirstAvailableData(pointOfSaleInfo.getPointOfSale(), tid.getForSoftpos(), tid.getForMpos()));
                }
            }
        }
        log.warn("no available terminals");
        return Optional.empty();
    }

}
