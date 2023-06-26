/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.microorchestrator.orchestrator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessStep
{
    private final String name;
    private Action action;
    private UndoAction undoAction;

    public ProcessStep(String name)
    {
        this.name = name;
    }

    public ProcessStep action(Action action)
    {
        this.action = action;
        return this;
    }

    public ProcessStep undoAction(UndoAction action)
    {
        this.undoAction = action;
        return this;
    }

    public void executeAction() throws Exception
    {
        log.debug("{}", this.name);
        if (this.action != null)
        {
            this.action.execute();
        }
    }

    public void executeUndoAction(Exception e)
    {
        log.debug("Rolling back {}", this.name);
        if (this.undoAction != null)
        {
            this.undoAction.execute(e);
        }
    }
}
