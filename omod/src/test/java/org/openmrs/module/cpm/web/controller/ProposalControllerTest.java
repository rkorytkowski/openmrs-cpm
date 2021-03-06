package org.openmrs.module.cpm.web.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cpm.PackageStatus;
import org.openmrs.module.cpm.ProposedConceptPackage;
import org.openmrs.module.cpm.api.ProposedConceptService;
import org.openmrs.module.cpm.web.dto.ProposedConceptPackageDto;
import org.openmrs.module.cpm.web.dto.concept.ConceptDto;
import org.openmrs.module.cpm.web.dto.concept.SearchConceptResultDto;
import org.openmrs.module.cpm.web.dto.factory.DescriptionDtoFactory;
import org.openmrs.module.cpm.web.dto.factory.NameDtoFactory;
import org.openmrs.module.cpm.web.dto.validator.ConceptDtoValidator;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, LocaleUtility.class})
public class ProposalControllerTest {

	@Mock
	ProposedConceptService service;

    @Mock
    ConceptService  conceptService;

    @Mock
	ProposedConceptPackage conceptPackage;

	@Mock
	SubmitProposal submitProposal;

	@Mock
	UpdateProposedConceptPackage updateProposedConceptPackage;

    @Mock
    ConceptDtoValidator conceptDtoValidator;

    @InjectMocks
    ProposalController controller = new ProposalController(submitProposal, updateProposedConceptPackage,
            new DescriptionDtoFactory(), new NameDtoFactory(), conceptDtoValidator );

	@Before
	public void before() throws Exception {
		mockStatic(Context.class);
        mockStatic(LocaleUtility.class);

		PowerMockito.when(Context.class, "getService", ProposedConceptService.class).thenReturn(service);
		when(service.getProposedConceptPackageById(1)).thenReturn(conceptPackage);

        when(conceptDtoValidator.validate(any(ConceptDto.class))).thenReturn(true);
	}

	@Test
	public void updateProposal_sendProposal_shouldPersistChangesAndSendProposal() throws Exception {

		when(conceptPackage.getStatus()).thenReturn(PackageStatus.DRAFT);

		ProposedConceptPackageDto dto = new ProposedConceptPackageDto();
		dto.setStatus(PackageStatus.TBS);
		controller.updateProposal("1", dto);

		InOrder inOrder = inOrder(updateProposedConceptPackage, submitProposal);
		inOrder.verify(updateProposedConceptPackage).updateProposedConcepts(conceptPackage, dto);
		inOrder.verify(submitProposal).submitProposedConcept(conceptPackage);
	}

	@Test
	public void updateProposal_saveProposal_shouldPersistChanges() {

		ProposedConceptPackageDto dto = new ProposedConceptPackageDto();
		controller.updateProposal("1", dto);

		verify(updateProposedConceptPackage).updateProposedConcepts(conceptPackage, dto);
		verify(submitProposal, times(0)).submitProposedConcept(conceptPackage);
    }

    @Test
    public void shouldFindConcepts() throws  Exception{


        ConceptSearchResult result = new ConceptSearchResult();
        Concept concept = new Concept();
        concept.setConceptId(123);
        concept.setDateChanged(new Date());
        concept.setId(111);
        ConceptName conceptName = new ConceptName();
        conceptName.setName("My concept name");
        conceptName.setLocale(Locale.UK);
        Collection<ConceptName>  conceptNames = Lists.newArrayList();
        concept.setNames(conceptNames);
        ConceptDatatype datatype = new ConceptDatatype();
        datatype.setName("my datatype");
        concept.setPreferredName(conceptName);
        concept.setDatatype(datatype);
        Collection<ConceptDescription> descriptions = Lists.newArrayList();
        concept.setDescriptions(descriptions);

        result.setConcept(concept);

        List<ConceptSearchResult> resultList = Lists.newArrayList(result);

        PowerMockito.when(Context.class, "getConceptService").thenReturn(conceptService);

        PowerMockito.when(LocaleUtility.class, "getLocalesInOrder").thenReturn(Sets.newHashSet(Locale.US));

        when(conceptService.getConcepts(anyString(), any(Locale.class), anyBoolean())).thenReturn(resultList);


        String requestNum = "100";
        SearchConceptResultDto resultDto = controller.findConcepts("dummyQuery", requestNum);

        Assert.assertEquals(resultDto.getRequestNum(), requestNum);
        Assert.assertEquals(resultDto.getConcepts().size(), resultList.size());
        Assert.assertTrue(resultDto.getConcepts().get(0).getId() == concept.getId());


    }
}
