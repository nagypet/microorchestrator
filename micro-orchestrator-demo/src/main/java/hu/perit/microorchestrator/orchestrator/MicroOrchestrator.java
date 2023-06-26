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

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MicroOrchestrator
{
    private final List<ProcessStep> steps = new ArrayList<>();

    public void addStep(ProcessStep step)
    {
        this.steps.add(step);
    }

    public void execute() throws Exception
    {
        for (int i = 0; i < this.steps.size(); i++)
        {
            ProcessStep step = this.steps.get(i);
            try
            {
                step.executeAction();
            }
            catch (Exception e)
            {
                if (i > 0)
                {
                    log.warn("Process step {}/{} thrown exception, rolling back...", i, this.steps.size());
                    undoAllFrom(i - 1, e);
                }
                throw e;
            }
        }
        log.info("{} process step executed successfully", this.steps.size());
    }

    private void undoAllFrom(int from, Exception e)
    {
        for (int i = from; i >= 0; i--)
        {
            ProcessStep step = this.steps.get(i);
            step.executeUndoAction(e);
        }
    }
}
