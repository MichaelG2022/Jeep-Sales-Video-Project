package com.promineotech.jeep.service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.promineotech.jeep.dao.JeepSalesDao;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultJeepSalesService implements JeepSalesService {

  @Autowired
  private JeepSalesDao jeepSalesDao;

  /*
   * As far as I can tell, @Transactional acts as a Transaction manager handling rollbacks and
   * commits as needed like we did in MySQL Projects.
   * 
   * @Transactional tells the transaction manager to start and manage a transaction.
   * 
   * readOnly means the DB can't be modified (Create/Post, Update/Put, Delete/Delete)
   */
  @Transactional(readOnly = true)
  @Override
  public List<Jeep> fetchJeeps(JeepModel model, String trim) {
    log.info("The Service fetchJeeps method was called with model={} and trim={}", model, trim);

    List<Jeep> jeeps = jeepSalesDao.fetchJeeps(model, trim);
    
    if(jeeps.isEmpty()) {
      String msg = String.format("No jeeps were found with model=%s and trim=%s", model, trim);
      
      throw new NoSuchElementException(msg);
    } // end IF

    Collections.sort(jeeps);

    return jeeps;
  } // end fetchJeeps

} // end CLASS
