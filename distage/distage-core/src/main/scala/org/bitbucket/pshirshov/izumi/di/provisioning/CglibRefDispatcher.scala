package org.bitbucket.pshirshov.izumi.di.provisioning

import java.util.concurrent.atomic.AtomicReference

import net.sf.cglib.proxy.ProxyRefDispatcher
import org.bitbucket.pshirshov.izumi.di.commons.Value
import org.bitbucket.pshirshov.izumi.di.model.DIKey
import org.bitbucket.pshirshov.izumi.di.model.exceptions.MissingRefException

class CglibRefDispatcher(key: DIKey) extends ProxyRefDispatcher {
  val reference = new AtomicReference[AnyRef](null)

  override def loadObject(o: scala.Any): AnyRef = {
    Value(reference.get())
        .eff(checkNotNull)
      .get
  }

  protected def checkNotNull(ref: AnyRef): Unit = {
    if (ref == null) {
      throw new MissingRefException(s"Proxy $key is not yet initialized", Set(key), None)
    }
  }
}
