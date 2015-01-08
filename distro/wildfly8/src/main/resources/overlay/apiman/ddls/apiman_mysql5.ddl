
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB;
INSERT INTO `hibernate_sequence` VALUES (1);

CREATE TABLE `auditlog` (
  `id` bigint(20) NOT NULL,
  `createdOn` datetime NOT NULL,
  `data` longtext,
  `entityId` varchar(255) DEFAULT NULL,
  `entityType` varchar(255) NOT NULL,
  `entityVersion` varchar(255) DEFAULT NULL,
  `organizationId` varchar(255) NOT NULL,
  `what` varchar(255) NOT NULL,
  `who` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;
CREATE INDEX `IDX_auditlog_1` ON `auditlog` (`who`);
CREATE INDEX `IDX_auditlog_2` ON `auditlog` (`organizationId`, `entityId`, `entityVersion`, `entityType`);

CREATE TABLE `plugins` (
  `id` bigint(20) NOT NULL,
  `groupId` varchar(255) NOT NULL,
  `artifactId` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  `classifier` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  UNIQUE KEY `UK_plugins_1` (`groupId`, `artifactId`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `gateways` (
  `id` varchar(255) NOT NULL,
  `configuration` longtext NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `modifiedBy` varchar(255) NOT NULL,
  `modifiedOn` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `policydefs` (
  `id` varchar(255) NOT NULL,
  `description` varchar(512) NOT NULL,
  `icon` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `policyImpl` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `pd_templates` (
  `policydef_id` varchar(255) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `template` varchar(2048) DEFAULT NULL,
  KEY `FK_pd_templates_1` (`policydef_id`),
  CONSTRAINT `FK_pd_templates_1` FOREIGN KEY (`policydef_id`) REFERENCES `policydefs` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `users` (
  `username` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fullName` varchar(255) DEFAULT NULL,
  `joinedOn` datetime DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB;
CREATE INDEX `IDX_users_1` ON `users` (`username`);
CREATE INDEX `IDX_users_2` ON `users` (`fullName`);

CREATE TABLE `roles` (
  `id` varchar(255) NOT NULL,
  `autoGrant` bit(1) DEFAULT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `permissions` (
  `role_id` varchar(255) NOT NULL,
  `permissions` int(11) DEFAULT NULL,
  KEY `FK_permissions_1` (`role_id`),
  CONSTRAINT `FK_permissions_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `memberships` (
  `id` bigint(20) NOT NULL,
  `createdOn` datetime DEFAULT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `role_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_memberships_1` (`user_id`,`role_id`,`org_id`)
) ENGINE=InnoDB;
CREATE INDEX `IDX_memberships_1` ON `memberships` (`user_id`);

CREATE TABLE `organizations` (
  `id` varchar(255) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `modifiedBy` varchar(255) NOT NULL,
  `modifiedOn` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;
CREATE INDEX `IDX_organizations_1` ON `organizations` (`name`);

CREATE TABLE `plans` (
  `id` varchar(255) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `organizationId` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`organizationId`),
  KEY `FK_plans_1` (`organizationId`),
  CONSTRAINT `FK_plans_1` FOREIGN KEY (`organizationId`) REFERENCES `organizations` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `plan_versions` (
  `id` bigint(20) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `lockedOn` datetime DEFAULT NULL,
  `modifiedBy` varchar(255) NOT NULL,
  `modifiedOn` datetime NOT NULL,
  `status` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  `plan_id` varchar(255) DEFAULT NULL,
  `plan_orgId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_plan_versions_1` (`plan_id`,`plan_orgId`,`version`),
  CONSTRAINT `FK_plan_versions_1` FOREIGN KEY (`plan_id`, `plan_orgId`) REFERENCES `plans` (`id`, `organizationId`)
) ENGINE=InnoDB;

CREATE TABLE `applications` (
  `id` varchar(255) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `organizationId` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`organizationId`),
  KEY `FK_applications_1` (`organizationId`),
  CONSTRAINT `FK_applications_1` FOREIGN KEY (`organizationId`) REFERENCES `organizations` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `application_versions` (
  `id` bigint(20) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `modifiedBy` varchar(255) NOT NULL,
  `modifiedOn` datetime NOT NULL,
  `publishedOn` datetime DEFAULT NULL,
  `retiredOn` datetime DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  `app_id` varchar(255) DEFAULT NULL,
  `app_orgId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_app_versions_1` (`app_id`,`app_orgId`,`version`),
  CONSTRAINT `FK_app_versions_1` FOREIGN KEY (`app_id`, `app_orgId`) REFERENCES `applications` (`id`, `organizationId`)
) ENGINE=InnoDB;

CREATE TABLE `services` (
  `id` varchar(255) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `organizationId` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`organizationId`),
  KEY `FK_services_1` (`organizationId`),
  CONSTRAINT `FK_services_1` FOREIGN KEY (`organizationId`) REFERENCES `organizations` (`id`)
) ENGINE=InnoDB;
CREATE INDEX `IDX_services_1` ON `services` (`name`);

CREATE TABLE `service_versions` (
  `id` bigint(20) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `endpoint` varchar(255) DEFAULT NULL,
  `endpointType` varchar(255) DEFAULT NULL,
  `modifiedBy` varchar(255) NOT NULL,
  `modifiedOn` datetime NOT NULL,
  `publishedOn` datetime DEFAULT NULL,
  `retiredOn` datetime DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `version` varchar(255) DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  `service_orgId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_service_versions_1` (`service_id`,`service_orgId`,`version`),
  CONSTRAINT `FK_service_versions_1` FOREIGN KEY (`service_id`, `service_orgId`) REFERENCES `services` (`id`, `organizationId`)
) ENGINE=InnoDB;

CREATE TABLE `svc_gateways` (
  `service_version_id` bigint(20) NOT NULL,
  `gatewayId` varchar(255) NOT NULL,
  PRIMARY KEY (`service_version_id`,`gatewayId`),
  CONSTRAINT `FK_svc_gateways_1` FOREIGN KEY (`service_version_id`) REFERENCES `service_versions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `svc_plans` (
  `service_version_id` bigint(20) NOT NULL,
  `planId` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  PRIMARY KEY (`service_version_id`,`planId`,`version`),
  CONSTRAINT `FK_svc_plans_1` FOREIGN KEY (`service_version_id`) REFERENCES `service_versions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `policies` (
  `id` bigint(20) NOT NULL,
  `configuration` longtext,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `entityId` varchar(255) NOT NULL,
  `entityVersion` varchar(255) NOT NULL,
  `modifiedBy` varchar(255) NOT NULL,
  `modifiedOn` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `orderIndex` int(11) NOT NULL,
  `organizationId` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `definition_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_policies_1` (`definition_id`),
  CONSTRAINT `FK_policies_1` FOREIGN KEY (`definition_id`) REFERENCES `policydefs` (`id`)
) ENGINE=InnoDB;
CREATE INDEX `IDX_policies_1` ON `policies` (`organizationId`, `entityId`, `entityVersion`, `type`);
CREATE INDEX `IDX_policies_2` ON `policies` (`orderIndex`);

CREATE TABLE `contracts` (
  `id` bigint(20) NOT NULL,
  `apikey` varchar(255) NOT NULL,
  `createdBy` varchar(255) NOT NULL,
  `createdOn` datetime NOT NULL,
  `appv_id` bigint(20) DEFAULT NULL,
  `planv_id` bigint(20) DEFAULT NULL,
  `svcv_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_contracts_1` (`appv_id`,`svcv_id`,`planv_id`),
  KEY `FK_contracts_p` (`planv_id`),
  KEY `FK_contracts_s` (`svcv_id`),
  KEY `FK_contracts_a` (`appv_id`),
  CONSTRAINT `FK_contracts_p` FOREIGN KEY (`planv_id`) REFERENCES `plan_versions` (`id`),
  CONSTRAINT `FK_contracts_s` FOREIGN KEY (`svcv_id`) REFERENCES `service_versions` (`id`),
  CONSTRAINT `FK_contracts_a` FOREIGN KEY (`appv_id`) REFERENCES `application_versions` (`id`)
) ENGINE=InnoDB;
