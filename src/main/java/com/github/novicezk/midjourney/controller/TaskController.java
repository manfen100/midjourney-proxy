package com.github.novicezk.midjourney.controller;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.dto.TaskConditionDTO;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Api(tags = "任务查询")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskStoreService taskStoreService;
    private final DiscordLoadBalancer discordLoadBalancer;
    private final ProxyProperties properties;

    @ApiOperation(value = "指定ID获取任务")
    @GetMapping("/{id}/fetch")
    public Task fetch(@ApiParam(value = "任务ID") @PathVariable String id) {
        log.info("{id}/fetch {}", id);
        Task task = this.taskStoreService.get(id);
        if(ObjectUtil.isNotNull(task)){
            task.setImageUrl(imgUrlChange(task.getImageUrl()));
        }
        return task;
    }

	@ApiOperation(value = "查询任务队列")
	@GetMapping("/queue")
	public List<Task> queue() {
		return this.discordLoadBalancer.getQueueTaskIds().stream()
				.map(this.taskStoreService::get).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Task::getSubmitTime))
				.toList();
	}

	@ApiOperation(value = "查询所有任务")
	@GetMapping("/list")
	public List<Task> list() {
		return this.taskStoreService.list().stream()
				.sorted((t1, t2) -> CompareUtil.compare(t2.getSubmitTime(), t1.getSubmitTime()))
				.toList();
	}

	@ApiOperation(value = "根据ID列表查询任务")
	@PostMapping("/list-by-condition")
	public List<Task> listByIds(@RequestBody TaskConditionDTO conditionDTO) {
		if (conditionDTO.getIds() == null) {
			return Collections.emptyList();
		}
		return conditionDTO.getIds().stream().map(this.taskStoreService::get).filter(Objects::nonNull).toList();
	}

    private String imgUrlChange(String imgUrl) {

        if (StrUtil.isBlank(imgUrl)) {
            return imgUrl;
        }
        log.info("task传入的url:{}", imgUrl);
        String newurl = StrUtil.replace(imgUrl, properties.getImgProxy().getExitdomain(), properties.getImgProxy().getPredomain());
        log.info("task替换域名,{}", newurl);
//        int index = newurl.indexOf("?");
//        String result = (index != -1) ? newurl.substring(0, index) : newurl;
//        log.info("task去掉后缀,{}", result);
        return result;
    }

}
